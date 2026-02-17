package org.sammomanyi.mediaccess.features.queue.data.desktop

import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueEntry
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueStatus
import java.util.UUID

class QueueDesktopRepository(
    private val firestoreClient: FirestoreRestClient
) {
    // Get all queue entries for a specific doctor today
    suspend fun getDoctorQueue(doctorId: String, date: String): List<QueueEntry> {
        return try {
            val docs = firestoreClient.getCollection("queue_entries")
            docs
                .map { (id, fields) -> fieldsToQueueEntry(id, fields) }
                .filter { it.doctorId == doctorId && it.date == date }
                .filter { it.status != QueueStatus.DONE.name }
                .sortedBy { it.queuePosition }
        } catch (e: Exception) {
            println("🔴 QueueDesktopRepository.getDoctorQueue error: ${e.message}")
            emptyList()
        }
    }

    // Get completed entries for today (doctor history panel)
    suspend fun getDoctorCompletedToday(doctorId: String, date: String): List<QueueEntry> {
        return try {
            val docs = firestoreClient.getCollection("queue_entries")
            docs
                .map { (id, fields) -> fieldsToQueueEntry(id, fields) }
                .filter { it.doctorId == doctorId && it.date == date && it.status == QueueStatus.DONE.name }
                .sortedByDescending { it.completedAt ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Called by receptionist after verifying check-in code
    suspend fun addToQueue(
        patientUserId: String,
        patientName: String,
        patientEmail: String,
        visitCodeId: String,
        purpose: String,
        doctor: org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount,
        insuranceName: String,
        memberNumber: String
    ): Result<QueueEntry> {
        return try {
            val date = org.sammomanyi.mediaccess.features.queue.data.QueueRepository.todayString()

            // Count existing WAITING entries for this doctor today to compute position
            val existingQueue = getDoctorQueue(doctor.id, date)
            val nextPosition = (existingQueue.maxOfOrNull { it.queuePosition } ?: 0) + 1

            val entry = QueueEntry(
                id = UUID.randomUUID().toString(),
                patientUserId = patientUserId,
                patientName = patientName,
                patientEmail = patientEmail,
                visitCodeId = visitCodeId,
                purpose = purpose,
                doctorId = doctor.id,
                doctorName = doctor.name,
                roomNumber = doctor.roomNumber,
                status = QueueStatus.WAITING.name,
                queuePosition = nextPosition,
                insuranceName = insuranceName,
                memberNumber = memberNumber,
                assignedAt = Clock.System.now().toEpochMilliseconds(),
                date = date
            )

            firestoreClient.updateDocument(
                collection = "queue_entries",
                documentId = entry.id,
                fields = mapOf(
                    "patientUserId" to entry.patientUserId,
                    "patientName" to entry.patientName,
                    "patientEmail" to entry.patientEmail,
                    "visitCodeId" to entry.visitCodeId,
                    "purpose" to entry.purpose,
                    "doctorId" to entry.doctorId,
                    "doctorName" to entry.doctorName,
                    "roomNumber" to entry.roomNumber,
                    "status" to entry.status,
                    "queuePosition" to entry.queuePosition,
                    "insuranceName" to entry.insuranceName,
                    "memberNumber" to entry.memberNumber,
                    "assignedAt" to entry.assignedAt,
                    "calledAt" to null,
                    "completedAt" to null,
                    "date" to entry.date
                )
            )
            Result.success(entry)
        } catch (e: Exception) {
            println("🔴 QueueDesktopRepository.addToQueue error: ${e.message}")
            Result.failure(e)
        }
    }

    // Doctor marks patient as done → advance next patient to IN_PROGRESS
    suspend fun markPatientDone(entryId: String, doctorId: String, date: String): Result<Unit> {
        return try {
            val now = Clock.System.now().toEpochMilliseconds()

            // 1. Mark this entry as DONE
            firestoreClient.updateDocument(
                collection = "queue_entries",
                documentId = entryId,
                fields = mapOf(
                    "status" to QueueStatus.DONE.name,
                    "completedAt" to now
                )
            )

            // 2. Get remaining WAITING entries for this doctor
            val remaining = getDoctorQueue(doctorId, date)
                .filter { it.status == QueueStatus.WAITING.name }
                .sortedBy { it.queuePosition }

            // 3. Promote the first WAITING to IN_PROGRESS
            if (remaining.isNotEmpty()) {
                firestoreClient.updateDocument(
                    collection = "queue_entries",
                    documentId = remaining.first().id,
                    fields = mapOf(
                        "status" to QueueStatus.IN_PROGRESS.name,
                        "calledAt" to now,
                        "queuePosition" to 1
                    )
                )
                // Re-number remaining WAITING entries starting at 2
                remaining.drop(1).forEachIndexed { idx, entry ->
                    firestoreClient.updateDocument(
                        collection = "queue_entries",
                        documentId = entry.id,
                        fields = mapOf("queuePosition" to idx + 2)
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("🔴 QueueDesktopRepository.markPatientDone error: ${e.message}")
            Result.failure(e)
        }
    }

    // Doctor calls a specific patient (marks IN_PROGRESS manually)
    suspend fun callPatient(entryId: String): Result<Unit> {
        return try {
            firestoreClient.updateDocument(
                collection = "queue_entries",
                documentId = entryId,
                fields = mapOf(
                    "status" to QueueStatus.IN_PROGRESS.name,
                    "calledAt" to Clock.System.now().toEpochMilliseconds()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fieldsToQueueEntry(id: String, fields: Map<String, Any?>): QueueEntry {
        return QueueEntry(
            id = id,
            patientUserId = fields["patientUserId"]?.toString() ?: "",
            patientName = fields["patientName"]?.toString() ?: "",
            patientEmail = fields["patientEmail"]?.toString() ?: "",
            visitCodeId = fields["visitCodeId"]?.toString() ?: "",
            purpose = fields["purpose"]?.toString() ?: "",
            doctorId = fields["doctorId"]?.toString() ?: "",
            doctorName = fields["doctorName"]?.toString() ?: "",
            roomNumber = fields["roomNumber"]?.toString() ?: "",
            status = fields["status"]?.toString() ?: QueueStatus.WAITING.name,
            queuePosition = (fields["queuePosition"] as? Long)?.toInt() ?: 0,
            insuranceName = fields["insuranceName"]?.toString() ?: "",
            memberNumber = fields["memberNumber"]?.toString() ?: "",
            assignedAt = (fields["assignedAt"] as? Long) ?: 0L,
            calledAt = fields["calledAt"] as? Long,
            completedAt = fields["completedAt"] as? Long,
            date = fields["date"]?.toString() ?: ""
        )
    }
}
package org.sammomanyi.mediaccess.features.queue.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.pharmacy.data.PharmacyQueueRepository
import org.sammomanyi.mediaccess.features.pharmacy.data.PrescriptionRepository
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Prescription
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionItem
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionStatus
import org.sammomanyi.mediaccess.features.queue.data.QueueRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.QueueDesktopRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueEntry
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueStatus


private val prescriptionRepository: PrescriptionRepository = get()
private val pharmacyQueueRepository: PharmacyQueueRepository = get()
data class DoctorQueueState(
    val waitingQueue: List<QueueEntry> = emptyList(),
    val currentPatient: QueueEntry? = null,
    val completedToday: List<QueueEntry> = emptyList(),
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val error: String? = null,
    val doctorName: String = "",
    val lastRefreshedAt: Long? = null,
    val isAvailable: Boolean = true , // ← NEW
    val showPrescriptionDialog: Boolean = false,
    val selectedPatientForPrescription: QueueEntry? = null
)

class DoctorQueueViewModel(
    private val queueRepository: QueueDesktopRepository,
    private val staffRepository: StaffFirestoreRepository,  // ← NEW param
    private val doctor: AdminAccountEntity
) : ViewModel() {

    private val _state = MutableStateFlow(DoctorQueueState(
        doctorName = doctor.name,
        isAvailable = true  // default to available on login
    ))
    val state: StateFlow<DoctorQueueState> = _state.asStateFlow()

    private var autoPollJob: Job? = null

    init {
        refresh()
        startAutoPoll()
    }

    // ✅ NEW: Toggle availability
    fun toggleAvailability() {
        viewModelScope.launch {
            val newStatus = !_state.value.isAvailable
            _state.update { it.copy(isAvailable = newStatus) }
            staffRepository.setOnDuty(doctor.id, newStatus)
        }
    }

    // ... rest of the ViewModel stays the same


    // Auto-refresh every 30 seconds (no real-time listener on desktop)
    private fun startAutoPoll() {
        autoPollJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val date = QueueRepository.todayString()
            val allActive = queueRepository.getDoctorQueue(doctor.id, date)
            val completed = queueRepository.getDoctorCompletedToday(doctor.id, date)

            val current = allActive.firstOrNull { it.status == QueueStatus.IN_PROGRESS.name }
            val waiting = allActive.filter { it.status == QueueStatus.WAITING.name }
                .sortedBy { it.queuePosition }

            _state.value = _state.value.copy(
                currentPatient = current,
                waitingQueue = waiting,
                completedToday = completed,
                isLoading = false,
                lastRefreshedAt = System.currentTimeMillis()
            )
        }
    }

    // Doctor taps ✓ Done — dismisses current patient, promotes next
    fun markDone() {
        val current = _state.value.currentPatient ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true)
            val date = QueueRepository.todayString()
            val result = queueRepository.markPatientDone(current.id, doctor.id, date)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(actionInProgress = false)
                    refresh()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        actionInProgress = false,
                        error = "Could not mark as done: ${e.message}"
                    )
                }
            )
        }
    }

    // Doctor manually calls a specific waiting patient
    fun callPatient(entryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true)
            queueRepository.callPatient(entryId)
            _state.value = _state.value.copy(actionInProgress = false)
            refresh()
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        autoPollJob?.cancel()
    }

    fun showPrescriptionDialog(patient: QueueEntry) {
        _state.update { it.copy(
            showPrescriptionDialog = true,
            selectedPatientForPrescription = patient
        ) }
    }

    fun dismissPrescriptionDialog() {
        _state.update { it.copy(
            showPrescriptionDialog = false,
            selectedPatientForPrescription = null
        ) }
    }

    fun createPrescription(
        medications: List<PrescriptionItem>,
        notes: String
    ) {
        val patient = _state.value.selectedPatientForPrescription ?: return
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }

            val prescription = Prescription(
                id = "",
                patientUserId = patient.patientUserId,
                patientName = patient.patientName,
                patientEmail = patient.patientEmail,
                doctorId = doctor.id,
                doctorName = doctor.name,
                queueEntryId = patient.id,
                medications = medications,
                notes = notes,
                status = PrescriptionStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                date = QueueRepository.todayString()
            )

            prescriptionRepository.createPrescription(prescription).fold(
                onSuccess = { prescriptionId ->
                    // Add to pharmacy queue
                    pharmacyQueueRepository.addToPharmacyQueue(
                        patientUserId = patient.patientUserId,
                        patientName = patient.patientName,
                        patientEmail = patient.patientEmail,
                        prescriptionId = prescriptionId,
                        date = QueueRepository.todayString()
                    )

                    // Mark patient as done in doctor queue
                    queueRepository.markPatientDone(patient.id, doctor.id, QueueRepository.todayString())

                    _state.update { it.copy(
                        actionInProgress = false,
                        showPrescriptionDialog = false,
                        selectedPatientForPrescription = null
                    ) }
                    refresh()
                },
                onFailure = { e ->
                    _state.update { it.copy(
                        actionInProgress = false,
                        error = "Failed to create prescription: ${e.message}"
                    ) }
                }
            )
        }
    }

}
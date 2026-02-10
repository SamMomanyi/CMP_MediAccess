package org.sammomanyi.mediaccess.features.identity.data.mappers

import org.sammomanyi.mediaccess.features.identity.data.local.MedicalRecordEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord

fun MedicalRecordEntity.toDomain(): MedicalRecord {
    return MedicalRecord(
        id = id,
        patientId = patientId,
        patientName = patientName,
        diagnosis = diagnosis,
        symptoms = symptoms,
        prescription = prescription,
        doctorName = doctorName,
        hospital = hospital,
        visitDate = visitDate,
        followUpDate = followUpDate,
        notes = notes,
        createdAt = createdAt
    )
}

fun MedicalRecord.toEntity(): MedicalRecordEntity {
    return MedicalRecordEntity(
        id = id,
        patientId = patientId,
        patientName = patientName,
        diagnosis = diagnosis,
        symptoms = symptoms,
        prescription = prescription,
        doctorName = doctorName,
        hospital = hospital,
        visitDate = visitDate,
        followUpDate = followUpDate,
        notes = notes,
        createdAt = createdAt
    )
}
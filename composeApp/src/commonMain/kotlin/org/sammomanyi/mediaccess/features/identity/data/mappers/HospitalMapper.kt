package org.sammomanyi.mediaccess.features.identity.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sammomanyi.mediaccess.features.identity.data.local.HospitalEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

fun HospitalEntity.toDomain(): Hospital {
    return Hospital(
        id = id,
        name = name,
        address = address,
        city = city,
        phoneNumber = phoneNumber,
        email = email,
        latitude = latitude,
        longitude = longitude,
        specialties = Json.decodeFromString(specialties),
        operatingHours = operatingHours,
        emergencyServices = emergencyServices,
        rating = rating,
        imageUrl = imageUrl
    )
}

fun Hospital.toEntity(): HospitalEntity {
    return HospitalEntity(
        id = id,
        name = name,
        address = address,
        city = city,
        phoneNumber = phoneNumber,
        email = email,
        latitude = latitude,
        longitude = longitude,
        specialties = Json.encodeToString(specialties),
        operatingHours = operatingHours,
        emergencyServices = emergencyServices,
        rating = rating,
        imageUrl = imageUrl
    )
}
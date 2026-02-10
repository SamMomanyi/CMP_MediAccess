package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Hospital(
    val id: String = "",
    val name: String,
    val address: String,
    val city: String,
    val phoneNumber: String,
    val email: String,
    val latitude: Double,
    val longitude: Double,
    val specialties: List<String> = emptyList(),
    val operatingHours: String,
    val emergencyServices: Boolean = false,
    val rating: Double = 0.0,
    val imageUrl: String? = null
) {
    fun distanceFrom(userLat: Double, userLon: Double): Double {
        // Haversine formula for distance calculation
        val earthRadius = 6371.0 // km

        val dLat = Math.toRadians(latitude - userLat)
        val dLon = Math.toRadians(longitude - userLon)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    fun formattedDistance(userLat: Double, userLon: Double): String {
        val distance = distanceFrom(userLat, userLon)
        return if (distance < 1.0) {
            "${(distance * 1000).toInt()} m"
        } else {
            "%.1f km".format(distance)
        }
    }
}
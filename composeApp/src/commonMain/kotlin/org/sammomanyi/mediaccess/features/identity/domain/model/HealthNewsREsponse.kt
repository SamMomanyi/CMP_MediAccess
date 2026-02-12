package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The actual health.gov API structure
@Serializable
data class HealthNewsResponse(
    @SerialName("Result") val result: HealthNewsResult
)

@Serializable
data class HealthNewsResult(
    @SerialName("Resources") val resources: HealthNewsResources
)

@Serializable
data class HealthNewsResources(
    @SerialName("Resource") val resource: List<HealthResource> = emptyList()
)

@Serializable
data class HealthResource(
    @SerialName("Id") val id: String = "",
    @SerialName("Title") val title: String = "",
    @SerialName("ImageUrl") val imageUrl: String = "",
    @SerialName("AccessibleVersion") val accessibleVersion: String = "",
    @SerialName("Sections") val sections: HealthSections? = null
)

@Serializable
data class HealthSections(
    @SerialName("section") val section: List<HealthSection> = emptyList()
)

@Serializable
data class HealthSection(
    @SerialName("Title") val title: String = "",
    @SerialName("Content") val content: String = ""
)

@Serializable
data class Article(
    val id: String,
    val title: String,
    val imageUrl: String,
    val date: String,
    val contentUrl: String
)
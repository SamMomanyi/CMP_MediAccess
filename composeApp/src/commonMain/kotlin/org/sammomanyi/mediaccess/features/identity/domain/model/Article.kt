package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val date: String,
    val contentUrl: String,
    val category: String = "General"
)

// Helper for the API Response
@Serializable
data class HealthNewsResponse(
    val Result: HealthResult
)

@Serializable
data class HealthResult(
    val Resources: List<HealthResource>
)

@Serializable
data class HealthResource(
    val Id: String,
    val Title: String,
    val ImageUrl: String,
    val AccessibleVersion: String // The web link
)
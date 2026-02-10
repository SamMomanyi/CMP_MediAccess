package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthNewsResponse(
    val Result: HealthNewsResult
)

@Serializable
data class HealthNewsResult(
    val Resources: List<HealthResource>
)

@Serializable
data class HealthResource(
    val Id: String,
    val Title: String,
    val ImageUrl: String = "",
    val AccessibleVersion: String = ""
)

@Serializable
data class Article(
    val id: String,
    val title: String,
    val imageUrl: String,
    val date: String,
    val contentUrl: String
)
package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.datetime.Instant

data class VisitCode(
    val code: String,
    val userId: String,
    val expiresAt: Long // Timestamp in milliseconds
)
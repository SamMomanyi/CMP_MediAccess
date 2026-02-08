package org.sammomanyi.mediaccess.features.identity.domain.model


//import kotlin.time.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

data class VisitCode @OptIn(ExperimentalTime::class) constructor(
    val code: String,           // e.g., "ABC123" - 6 digit alphanumeric
    val userId: String,
    val generatedAt: Instant,
    val expiresAt: Instant,
    val purpose: VisitPurpose = VisitPurpose.GENERAL_VISIT,
    val usedAt: Instant? = null, // Track if/when code was used
    val isActive: Boolean = true // Can be revoked
) {
    @OptIn(ExperimentalTime::class)
    fun isExpired(): Boolean = Clock.System.now() > expiresAt

    @OptIn(ExperimentalTime::class)
    fun isValid(): Boolean = isActive && !isExpired() && usedAt == null

    @OptIn(ExperimentalTime::class)
    fun timeRemaining(): Long {
        val now = Clock.System.now()
        return if (now < expiresAt) {
            (expiresAt - now).inWholeSeconds
        } else 0L
    }

    companion object {
        val DEFAULT_VALIDITY = 15.minutes // OTP valid for 15 minutes

        @OptIn(ExperimentalTime::class)
        fun generate(
            userId: String,
            purpose: VisitPurpose = VisitPurpose.GENERAL_VISIT
        ): VisitCode {
            val now = Clock.System.now()
            return VisitCode(
                code = generateRandomCode(),
                userId = userId,
                generatedAt = now,
                expiresAt = now + DEFAULT_VALIDITY,
                purpose = purpose
            )
        }

        private fun generateRandomCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Avoid confusing chars
            return (1..6).map { chars.random() }.joinToString("")
        }
    }
}

enum class VisitPurpose {
    GENERAL_VISIT,
    EMERGENCY,
    PRESCRIPTION_PICKUP,
    LAB_RESULTS,
    CONSULTATION
}
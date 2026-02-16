package org.sammomanyi.mediaccess.features.verification.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.verification.data.desktop.CoverVerificationStatus
import org.sammomanyi.mediaccess.features.verification.data.desktop.VerifiedVisitResult
import org.sammomanyi.mediaccess.features.verification.data.desktop.VisitVerificationRestClient

data class VerificationHistoryEntry(
    val patientName: String,
    val patientEmail: String,
    val purpose: String,
    val insuranceName: String?,
    val coverStatus: CoverVerificationStatus,
    val verifiedAt: Long,
    val markedAsUsed: Boolean
)

data class VisitVerificationUiState(
    // Code input
    val codeInput: String = "",

    // Verification process
    val isVerifying: Boolean = false,
    val verificationResult: VerifiedVisitResult? = null,
    val verificationError: String? = null,

    // Mark as used
    val isMarkingUsed: Boolean = false,
    val markUsedSuccess: Boolean = false,

    // Today's history
    val todayHistory: List<VerificationHistoryEntry> = emptyList()
)

class VisitVerificationViewModel(
    private val client: VisitVerificationRestClient
) : ViewModel() {

    private val _state = MutableStateFlow(VisitVerificationUiState())
    val state: StateFlow<VisitVerificationUiState> = _state.asStateFlow()

    fun onCodeInputChanged(value: String) {
        val cleaned = value.uppercase().replace(" ", "").take(20)
        _state.value = _state.value.copy(
            codeInput = cleaned,
            verificationResult = null,
            verificationError = null,
            markUsedSuccess = false
        )
    }

    fun verifyCode() {
        val code = _state.value.codeInput.trim()
        if (code.isBlank()) {
            _state.value = _state.value.copy(verificationError = "Please enter a visit code.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isVerifying = true,
                verificationError = null,
                verificationResult = null,
                markUsedSuccess = false
            )

            val result = client.verifyCode(code)

            if (result.isFailure) {
                _state.value = _state.value.copy(
                    isVerifying = false,
                    verificationError = "Could not reach server: ${result.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val visitResult = result.getOrThrow()

            // Map any blocking states to clear error messages
            val error = when (visitResult.coverStatus) {
                CoverVerificationStatus.CODE_INVALID ->
                    "Code not found. Please double-check and try again."
                CoverVerificationStatus.CODE_ALREADY_USED ->
                    "This code has already been used at a previous visit."
                CoverVerificationStatus.CODE_EXPIRED ->
                    "This code has expired. Ask the patient to generate a new one."
                CoverVerificationStatus.COVER_PENDING ->
                    "Patient's cover is still pending admin approval. They cannot check in yet."
                CoverVerificationStatus.COVER_REJECTED ->
                    "Patient's cover request was rejected. They need to resubmit and get approval."
                CoverVerificationStatus.COVER_NONE ->
                    "Patient has no insurance cover on file. They need to submit a cover request first."
                CoverVerificationStatus.APPROVED ->
                    null   // ✓ All good — no error
            }

            _state.value = _state.value.copy(
                isVerifying = false,
                verificationResult = if (error == null) visitResult else null,
                verificationError = error
            )
        }
    }

    fun markAsUsed() {
        val result = _state.value.verificationResult ?: return
        // Extra safety check — only mark if cover is approved
        if (result.coverStatus != CoverVerificationStatus.APPROVED) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isMarkingUsed = true)

            val outcome = client.markCodeAsUsed(result.code)

            if (outcome.isSuccess) {
                val historyEntry = VerificationHistoryEntry(
                    patientName   = result.patientName,
                    patientEmail  = result.patientEmail,
                    purpose       = result.purpose,
                    insuranceName = result.insuranceName,
                    coverStatus   = result.coverStatus,
                    verifiedAt    = Clock.System.now().toEpochMilliseconds(),
                    markedAsUsed  = true
                )
                _state.value = _state.value.copy(
                    isMarkingUsed      = false,
                    markUsedSuccess    = true,
                    verificationResult = null,
                    codeInput          = "",
                    todayHistory       = listOf(historyEntry) + _state.value.todayHistory
                )
            } else {
                _state.value = _state.value.copy(
                    isMarkingUsed    = false,
                    verificationError = "Failed to mark as used: ${outcome.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(
            codeInput          = "",
            verificationResult = null,
            verificationError  = null,
            markUsedSuccess    = false
        )
    }
}
package org.sammomanyi.mediaccess.features.verification.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.sammomanyi.mediaccess.features.queue.data.desktop.QueueDesktopRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount
import org.sammomanyi.mediaccess.features.verification.data.desktop.VerifiedVisitResult
import org.sammomanyi.mediaccess.features.verification.data.desktop.VisitVerificationRestClient
import org.sammomanyi.mediaccess.features.verification.data.desktop.CoverVerificationStatus

data class VerificationHistoryEntry(
    val patientEmail: String,
    val purpose: String,
    val usedAt: Long,
    val insuranceName: String
)

data class VisitVerificationState(
    val codeInput: String = "",
    val isVerifying: Boolean = false,
    val verifiedResult: VerifiedVisitResult? = null,
    val verifyError: String? = null,

    // Doctor assignment step (shown after successful verify)
    val showDoctorPicker: Boolean = false,
    val onDutyDoctors: List<StaffAccount> = emptyList(),
    val selectedDoctor: StaffAccount? = null,
    val isLoadingDoctors: Boolean = false,
    val isAssigning: Boolean = false,
    val assignSuccess: Boolean = false,
    val assignError: String? = null,

    val todayHistory: List<VerificationHistoryEntry> = emptyList()
)

class VisitVerificationViewModel(
    private val verificationClient: VisitVerificationRestClient,
    private val staffRepository: StaffFirestoreRepository,
    private val queueRepository: QueueDesktopRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VisitVerificationState())
    val state: StateFlow<VisitVerificationState> = _state.asStateFlow()

    fun onCodeInputChanged(value: String) {
        _state.value = _state.value.copy(
            codeInput = value.uppercase().take(8),
            verifyError = null,
            verifiedResult = null,
            showDoctorPicker = false,
            selectedDoctor = null,
            assignSuccess = false,
            assignError = null
        )
    }

    fun verifyCode() {
        val code = _state.value.codeInput.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isVerifying = true, verifyError = null)

            // ✅ verifyCode returns Result<VerifiedVisitResult> — unwrap with getOrNull()
            val result = verificationClient.verifyCode(code).getOrNull()
            if (result == null) {
                _state.value = _state.value.copy(
                    isVerifying = false,
                    verifyError = "Code not found. Please check and try again."
                )
                return@launch
            }

            val errorMessage = when (result.coverStatus) {
                CoverVerificationStatus.CODE_EXPIRED -> "This code has expired."
                CoverVerificationStatus.CODE_ALREADY_USED -> "This code has already been used."
                CoverVerificationStatus.CODE_INVALID -> "Invalid code."
                CoverVerificationStatus.COVER_PENDING -> "Patient's cover is still pending approval."
                CoverVerificationStatus.COVER_REJECTED -> "Patient's cover was rejected."
                CoverVerificationStatus.COVER_NONE -> "Patient has no active cover."
                CoverVerificationStatus.APPROVED -> null
            }

            if (errorMessage != null) {
                _state.value = _state.value.copy(
                    isVerifying = false,
                    verifiedResult = result,
                    verifyError = errorMessage
                )
                return@launch
            }

            _state.value = _state.value.copy(
                isVerifying = false,
                verifiedResult = result,
                isLoadingDoctors = true
            )

            val doctors = staffRepository.getOnDutyDoctors()
            _state.value = _state.value.copy(
                isLoadingDoctors = false,
                showDoctorPicker = true,
                onDutyDoctors = doctors
            )
        }
    }

    fun confirmAssignment() {
        val result = _state.value.verifiedResult ?: return
        val doctor = _state.value.selectedDoctor ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isAssigning = true, assignError = null)

            // ✅ result.code is the code string, result.userId is the patient ID
            verificationClient.markCodeAsUsed(result.code)

            val queueResult = queueRepository.addToQueue(
                patientUserId = result.userId,          // ✅ was result.patientId
                patientName = result.patientName,
                patientEmail = result.patientEmail,
                visitCodeId = result.code,              // ✅ was result.visitCodeId
                purpose = result.purpose,
                doctor = doctor,
                insuranceName = result.insuranceName ?: "",
                memberNumber = result.memberNumber ?: ""
            )

            queueResult.fold(
                onSuccess = { _ ->
                    val historyEntry = VerificationHistoryEntry(
                        patientEmail = result.patientEmail,
                        purpose = result.purpose,
                        usedAt = System.currentTimeMillis(),
                        insuranceName = result.insuranceName ?: ""
                    )
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        assignSuccess = true,
                        showDoctorPicker = false,
                        todayHistory = listOf(historyEntry) + _state.value.todayHistory
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        assignError = "Assignment failed: ${e.message}"
                    )
                }
            )
        }
    }

    fun selectDoctor(doctor: StaffAccount) {
        _state.value = _state.value.copy(selectedDoctor = doctor)
    }

    fun resetForNextPatient() {
        _state.value = _state.value.copy(
            codeInput = "",
            verifiedResult = null,
            verifyError = null,
            showDoctorPicker = false,
            selectedDoctor = null,
            assignSuccess = false,
            assignError = null
        )
    }
}

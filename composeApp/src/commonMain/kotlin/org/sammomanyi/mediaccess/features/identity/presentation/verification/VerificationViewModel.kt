package org.sammomanyi.mediaccess.features.identity.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.VerifyAppointmentUseCase

class VerificationViewModel(
    private val verifyAppointmentUseCase: VerifyAppointmentUseCase,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationState())
    val state = _state.asStateFlow()

    init {
        loadPendingAppointments()
    }

    private fun loadPendingAppointments() {
        viewModelScope.launch {
            appointmentRepository.getPendingAppointments().collectLatest { appointments ->
                _state.update { it.copy(pendingAppointments = appointments) }
            }
        }
    }

    fun onAction(action: VerificationAction) {
        when (action) {
            is VerificationAction.OnCodeChange -> {
                _state.update {
                    it.copy(
                        visitCode = action.code.uppercase(),
                        errorMessage = null,
                        verifiedAppointment = null
                    )
                }
            }
            is VerificationAction.OnVerifierNameChange -> {
                _state.update { it.copy(verifierName = action.name) }
            }
            VerificationAction.OnVerifyClick -> {
                verifyCode(state.value.visitCode)
            }
            is VerificationAction.OnVerifyCode -> {
                _state.update { it.copy(visitCode = action.code) }
                verifyCode(action.code)
            }
        }
    }

    private fun verifyCode(code: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = verifyAppointmentUseCase(
                visitCode = code,
                verifiedBy = state.value.verifierName
            )

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            verifiedAppointment = result.data,
                            visitCode = "",
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.from(result.error)
                        )
                    }
                }
            }
        }
    }
}
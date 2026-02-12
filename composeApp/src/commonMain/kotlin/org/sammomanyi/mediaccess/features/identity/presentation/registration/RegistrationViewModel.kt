package org.sammomanyi.mediaccess.features.identity.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.Gender
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.UserRole
import org.sammomanyi.mediaccess.features.identity.domain.use_case.RegisterUserUseCase

class RegistrationViewModel(
    private val registerUserUseCase: RegisterUserUseCase  // Use the use case!
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.OnFirstNameChange -> {
                _state.value = _state.value.copy(
                    firstName = action.name,
                    firstNameError = null
                )
            }
            is RegistrationAction.OnLastNameChange -> {
                _state.value = _state.value.copy(
                    lastName = action.name,
                    lastNameError = null
                )
            }
            is RegistrationAction.OnEmailChange -> {
                _state.value = _state.value.copy(
                    email = action.email,
                    emailError = null
                )
            }
            is RegistrationAction.OnPasswordChange -> {
                _state.value = _state.value.copy(
                    password = action.password,
                    passwordError = null
                )
            }
            is RegistrationAction.OnPhoneNumberChange -> {
                _state.value = _state.value.copy(
                    phoneNumber = action.phone,
                    phoneError = null
                )
            }
            RegistrationAction.OnRegisterClick -> register()
            is RegistrationAction.OnConfirmPasswordChange -> {
                _state.value = _state.value.copy(
                    confirmPassword = action.password,
                    confirmPasswordError = null
                )
            }
        }
    }

    private fun register() {
        // Client-side validation FIRST
        if (!validateInput()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val medicalId = generateMedicalId()

            val user = User(
                id = "",
                firstName = _state.value.firstName.trim(),
                lastName = _state.value.lastName.trim(),
                email = _state.value.email.trim(),
                password = state.value.password,
                phoneNumber = _state.value.phoneNumber.trim(),
                dateOfBirth = "2000-01-01", // String format
                gender = Gender.PREFER_NOT_TO_SAY.name, // Enum to string
                role = UserRole.PATIENT.name, // Enum to string
                medicalId = medicalId,
                nationalId = null,
                balance = 0.0,
                profileImageUrl = null,
                isEmailVerified = false,
                createdAt = 0L
            )

            val result = registerUserUseCase(user, _state.value.password)

            when (result) {
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = UiText.from(result.error)
                    )
                }
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false

        if (_state.value.firstName.isBlank()) {
            _state.value = _state.value.copy(
                firstNameError = UiText.DynamicString("First name is required")
            )
            hasError = true
        }

        if (_state.value.lastName.isBlank()) {
            _state.value = _state.value.copy(
                lastNameError = UiText.DynamicString("Last name is required")
            )
            hasError = true
        }

        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(
                emailError = UiText.DynamicString("Email is required")
            )
            hasError = true
        }

        if (_state.value.phoneNumber.isBlank()) {
            _state.value = _state.value.copy(
                phoneError = UiText.DynamicString("Phone number is required")
            )
            hasError = true
        }

        if (_state.value.password.length < 8) {
            _state.value = _state.value.copy(
                passwordError = UiText.DynamicString("Password must be at least 8 characters")
            )
            hasError = true
        }

        return !hasError
    }

    private fun generateMedicalId(): String {
        // Simple implementation - you can make this more sophisticated
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "MED-$timestamp"
    }
}
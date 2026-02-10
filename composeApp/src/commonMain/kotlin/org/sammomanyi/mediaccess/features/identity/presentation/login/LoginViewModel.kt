package org.sammomanyi.mediaccess.features.identity.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GoogleSignInUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.LoginUserUseCase

class LoginViewModel(
    private val loginUserUseCase: LoginUserUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> {
                _state.value = _state.value.copy(
                    email = action.email,
                    emailError = null
                )
            }
            is LoginAction.OnPasswordChange -> {
                _state.value = _state.value.copy(
                    password = action.password,
                    passwordError = null
                )
            }
            LoginAction.OnLoginClick -> login()
            is LoginAction.OnGoogleSignIn -> handleGoogleSignIn(
                action.idToken,
                action.email,
                action.displayName,
                action.photoUrl
            )
        }
    }

    private fun login() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = loginUserUseCase(
                email = _state.value.email.trim(),
                password = _state.value.password
            )

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

    private fun handleGoogleSignIn(
        idToken: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = googleSignInUseCase(idToken, email, displayName, photoUrl)

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
        val email = _state.value.email.trim()
        val password = _state.value.password

        var hasError = false

        if (email.isBlank()) {
            _state.value = _state.value.copy(
                emailError = UiText.DynamicString("Email is required")
            )
            hasError = true
        }

        if (password.isBlank()) {
            _state.value = _state.value.copy(
                passwordError = UiText.DynamicString("Password is required")
            )
            hasError = true
        }

        return !hasError
    }
}
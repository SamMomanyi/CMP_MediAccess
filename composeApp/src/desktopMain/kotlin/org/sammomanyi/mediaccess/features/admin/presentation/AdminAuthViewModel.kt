package org.sammomanyi.mediaccess.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.admin.data.AdminRepository
import org.sammomanyi.mediaccess.features.admin.domain.model.Admin

sealed class AdminAuthState {
    object Idle : AdminAuthState()
    object Loading : AdminAuthState()
    data class LoggedIn(val admin: Admin) : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
}

class AdminAuthViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)
    val state: StateFlow<AdminAuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AdminAuthState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _state.value = AdminAuthState.Loading
            val result = adminRepository.login(email, password)
            _state.value = if (result.isSuccess) {
                AdminAuthState.LoggedIn(result.getOrThrow())
            } else {
                AdminAuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = AdminAuthState.Error("Please fill in all fields")
            return
        }
        if (password != confirmPassword) {
            _state.value = AdminAuthState.Error("Passwords do not match")
            return
        }
        if (password.length < 8) {
            _state.value = AdminAuthState.Error("Password must be at least 8 characters")
            return
        }
        viewModelScope.launch {
            _state.value = AdminAuthState.Loading
            val result = adminRepository.register(name, email, password)
            _state.value = if (result.isSuccess) {
                AdminAuthState.LoggedIn(result.getOrThrow())
            } else {
                AdminAuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun clearError() {
        if (_state.value is AdminAuthState.Error) {
            _state.value = AdminAuthState.Idle
        }
    }
}
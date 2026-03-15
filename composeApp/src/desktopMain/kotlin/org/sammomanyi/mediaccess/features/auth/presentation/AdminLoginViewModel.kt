package org.sammomanyi.mediaccess.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole

data class AdminLoginState(
    val email: String = "",
    val password: String = "",
    val selectedRole: StaffRole = StaffRole.RECEPTIONIST,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInAccount: AdminAccountEntity? = null
)

class AdminLoginViewModel(
    private val staffFirestoreRepository: StaffFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginState())
    val state = _state.asStateFlow()

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChanged(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onRoleSelected(role: StaffRole) = _state.update { it.copy(selectedRole = role, error = null) }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // ✅ Authenticate against Firestore
            val account = staffFirestoreRepository.authenticateStaff(
                email = s.email.trim(),
                password = s.password,
                role = s.selectedRole
            )

            if (account == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Invalid email, password, or role. Please check your credentials."
                    )
                }
                return@launch
            }

            // ✅ Set doctor/pharmacist as on duty
            if (account.role == StaffRole.DOCTOR.name ||
                account.role == StaffRole.PHARMACIST.name) {
                staffFirestoreRepository.setOnDuty(account.id, true)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    loggedInAccount = account,
                    error = null
                )
            }
        }
    }

    fun logout(account: AdminAccountEntity) {
        viewModelScope.launch {
            // ✅ Set doctor/pharmacist as off duty
            if (account.role == StaffRole.DOCTOR.name ||
                account.role == StaffRole.PHARMACIST.name) {
                staffFirestoreRepository.setOnDuty(account.id, false)
            }
            _state.value = AdminLoginState()
        }
    }
}
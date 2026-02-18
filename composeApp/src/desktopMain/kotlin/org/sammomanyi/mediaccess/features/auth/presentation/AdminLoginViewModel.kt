package org.sammomanyi.mediaccess.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.parseServerSetCookieHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountDao
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import java.security.MessageDigest

data class AdminLoginState(
    val email: String = "",
    val password: String = "",
    val selectedRole: StaffRole = StaffRole.RECEPTIONIST,  // ← NEW
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInAccount: AdminAccountEntity? = null
)

class AdminLoginViewModel(
    private val adminAccountDao: AdminAccountDao,
    private val staffFirestoreRepository: StaffFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginState())
    val state = _state.asStateFlow()

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value) }
    fun onPasswordChanged(value: String) = _state.update { it.copy(password = value) }
    fun onRoleSelected(role: StaffRole) = _state.update { it.copy(selectedRole = role) }  // ← NEW

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val hash = sha256(s.password)
            val account = adminAccountDao.getByEmail(s.email.trim().lowercase())

            // Verify account exists + password matches
            if (account == null || account.passwordHash != hash) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Invalid email or password"
                ) }
                return@launch
            }

            // ✅ NEW: Role verification — check if account has the selected role
            if (account.role != s.selectedRole.name) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "This account is not registered as ${s.selectedRole.name.lowercase()}. Please select the correct role."
                ) }
                return@launch
            }

            // If doctor/pharmacist, flip isOnDuty in Firestore
            if (account.role == StaffRole.DOCTOR.name ||
                account.role == StaffRole.PHARMACIST.name) {
                staffFirestoreRepository.setOnDuty(account.id, true)
            }

            _state.update { it.copy(isLoading = false, loggedInAccount = account) }
        }
    }

    fun logout(account: AdminAccountEntity) {
        viewModelScope.launch {
            if (account.role == StaffRole.DOCTOR.name ||
                account.role == StaffRole.PHARMACIST.name) {
                staffFirestoreRepository.setOnDuty(account.id, false)
            }
            _state.value = AdminLoginState()
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
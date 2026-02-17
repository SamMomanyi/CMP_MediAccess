package org.sammomanyi.mediaccess.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountDao
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import java.security.MessageDigest

data class AdminLoginState(
    val email: String = "",
    val password: String = "",
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

    fun onEmailChanged(value: String) = _state.run { value(state.value.copy(email = value)) }
    fun onPasswordChanged(value: String) = _state.run { value(state.value.copy(password = value)) }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Please enter email and password")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)

            val hash = sha256(s.password)
            val account = adminAccountDao.getByEmail(s.email.trim().lowercase())

            if (account == null || account.passwordHash != hash) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Invalid email or password"
                )
                return@launch
            }

            // If doctor/pharmacist, flip isOnDuty in Firestore
            if (account.role == StaffRole.DOCTOR.name ||
                account.role == StaffRole.PHARMACIST.name) {
                staffFirestoreRepository.setOnDuty(account.id, true)
            }

            _state.value = _state.value.copy(isLoading = false, loggedInAccount = account)
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
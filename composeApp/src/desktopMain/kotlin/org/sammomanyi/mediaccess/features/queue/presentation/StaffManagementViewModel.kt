package org.sammomanyi.mediaccess.features.queue.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountDao
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import java.security.MessageDigest
import java.util.UUID

data class StaffManagementState(
    val staffList: List<StaffAccount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val formState: StaffFormState = StaffFormState()
)

data class StaffFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: StaffRole = StaffRole.RECEPTIONIST,
    val roomNumber: String = "",
    val specialization: String = ""
)

class StaffManagementViewModel(
    private val staffRepository: StaffFirestoreRepository,
    private val adminAccountDao: AdminAccountDao  // ← ADD THIS
) : ViewModel() {

    private val _state = MutableStateFlow(StaffManagementState())
    val state: StateFlow<StaffManagementState> = _state.asStateFlow()

    init {
        loadStaff()
    }

    fun loadStaff() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val staff = staffRepository.getAllStaff()
            _state.update { it.copy(
                staffList = staff,
                isLoading = false
            ) }
        }
    }

    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _state.update { it.copy(
            showCreateDialog = false,
            formState = StaffFormState()
        ) }
    }

    fun updateFormField(field: String, value: Any) {
        val current = _state.value.formState
        val updated = when (field) {
            "name" -> current.copy(name = value as String)
            "email" -> current.copy(email = value as String)
            "password" -> current.copy(password = value as String)
            "role" -> current.copy(role = value as StaffRole)
            "roomNumber" -> current.copy(roomNumber = value as String)
            "specialization" -> current.copy(specialization = value as String)
            else -> current
        }
        _state.update { it.copy(formState = updated) }
    }

    fun createStaff() {
        val form = _state.value.formState

        if (form.name.isBlank() || form.email.isBlank() || form.password.isBlank()) {
            _state.update { it.copy(error = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val staffId = UUID.randomUUID().toString()

            // Create staff account object (without password)
            val staffAccount = StaffAccount(
                id = staffId,
                name = form.name,
                email = form.email.trim().lowercase(),
                role = form.role.name,
                roomNumber = form.roomNumber.takeIf { it.isNotBlank() },
                specialization = form.specialigit status
                        zation.takeIf { it.isNotBlank() },
                isOnDuty = false,
                lastSeenAt = 0L,
                passwordHash = ""  // Will be generated in repository
            )

            // 1. Create in Firestore (with password for hashing)
            staffRepository.createStaff(staffAccount, form.password).fold(
                onSuccess = {
                    // 2. ✅ Also create in Room database (for desktop login)
                    val passwordHash = sha256(form.password)
                    val adminEntity = AdminAccountEntity(
                        id = staffId,
                        name = form.name,
                        email = form.email.trim().lowercase(),
                        passwordHash = passwordHash,
                        role = form.role.name
                    )

                    adminAccountDao.insert(adminEntity)

                    _state.update { it.copy(
                        isLoading = false,
                        showCreateDialog = false,
                        formState = StaffFormState()
                    ) }
                    loadStaff()
                },
                onFailure = { e ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Failed to create staff: ${e.message}"
                    ) }
                }
            )
        }
    }

    fun deleteStaff(staffId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Delete from both Firestore and Room
            staffRepository.deleteStaff(staffId)
            adminAccountDao.deleteById(staffId)  // ← Use deleteById instead of delete

            _state.update { it.copy(isLoading = false) }
            loadStaff()
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
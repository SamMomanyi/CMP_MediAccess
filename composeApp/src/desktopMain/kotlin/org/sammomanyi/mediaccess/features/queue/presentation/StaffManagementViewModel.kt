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
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val newName: String = "",
    val newEmail: String = "",
    val newPassword: String = "",
    val newRole: StaffRole = StaffRole.RECEPTIONIST,
    val newRoomNumber: String = "",
    val newSpecialization: String = ""
)

class StaffManagementViewModel(
    private val staffRepository: StaffFirestoreRepository,
    private val adminAccountDao: AdminAccountDao
) : ViewModel() {

    private val _state = MutableStateFlow(StaffManagementState())
    val state: StateFlow<StaffManagementState> = _state.asStateFlow()

    init {
        loadStaff()
    }

    fun refresh() = loadStaff()

    private fun loadStaff() {
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
            newName = "",
            newEmail = "",
            newPassword = "",
            newRole = StaffRole.RECEPTIONIST,
            newRoomNumber = "",
            newSpecialization = "",
            error = null
        ) }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(newName = value) }
    }

    fun onEmailChanged(value: String) {
        _state.update { it.copy(newEmail = value) }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.copy(newPassword = value) }
    }

    fun onRoleChanged(value: StaffRole) {
        _state.update { it.copy(newRole = value) }
    }

    fun onRoomChanged(value: String) {
        _state.update { it.copy(newRoomNumber = value) }
    }

    fun onSpecializationChanged(value: String) {
        _state.update { it.copy(newSpecialization = value) }
    }

    fun createStaff() {
        val s = _state.value

        if (s.newName.isBlank() || s.newEmail.isBlank() || s.newPassword.isBlank()) {
            _state.update { it.copy(error = "Please fill all required fields") }
            return
        }

        if (s.newRole == StaffRole.DOCTOR && s.newRoomNumber.isBlank()) {
            _state.update { it.copy(error = "Room number is required for doctors") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }

            val staffId = UUID.randomUUID().toString()

            val staffAccount = StaffAccount(
                id = staffId,
                name = s.newName,
                email = s.newEmail.trim().lowercase(),
                role = s.newRole.name,
                roomNumber = s.newRoomNumber.ifBlank { "" },
                specialization = s.newSpecialization.ifBlank { "" },
                isOnDuty = false,
                lastSeenAt = 0L,
                passwordHash = ""
            )

            staffRepository.createStaff(staffAccount, s.newPassword).fold(
                onSuccess = {
                    // Also create in Room database
                    val passwordHash = sha256(s.newPassword)
                    val adminEntity = AdminAccountEntity(
                        id = staffId,
                        name = s.newName,
                        email = s.newEmail.trim().lowercase(),
                        passwordHash = passwordHash,
                        role = s.newRole.name
                    )

                    adminAccountDao.insert(adminEntity)

                    _state.update { it.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        successMessage = "Staff account created successfully",
                        newName = "",
                        newEmail = "",
                        newPassword = "",
                        newRole = StaffRole.RECEPTIONIST,
                        newRoomNumber = "",
                        newSpecialization = ""
                    ) }
                    loadStaff()
                },
                onFailure = { e ->
                    _state.update { it.copy(
                        isCreating = false,
                        error = "Failed to create staff: ${e.message}"
                    ) }
                }
            )
        }
    }

    fun deleteStaff(staff: StaffAccount) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Delete from both Firestore and Room
            staffRepository.deleteStaff(staff.id)
            adminAccountDao.deleteById(staff.id)

            _state.update { it.copy(
                isLoading = false,
                successMessage = "${staff.name} deleted successfully"
            ) }
            loadStaff()
        }
    }

    fun dismissFeedback() {
        _state.update { it.copy(error = null, successMessage = null) }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
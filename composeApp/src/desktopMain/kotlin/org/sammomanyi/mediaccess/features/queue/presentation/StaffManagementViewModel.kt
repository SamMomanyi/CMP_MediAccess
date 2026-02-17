package org.sammomanyi.mediaccess.features.queue.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val successMessage: String? = null,
    // Create dialog
    val showCreateDialog: Boolean = false,
    val newName: String = "",
    val newEmail: String = "",
    val newPassword: String = "",
    val newRole: StaffRole = StaffRole.DOCTOR,
    val newRoomNumber: String = "",
    val newSpecialization: String = "",
    val isCreating: Boolean = false
)

class StaffManagementViewModel(
    private val adminAccountDao: AdminAccountDao,
    private val staffFirestoreRepository: StaffFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffManagementState())
    val state: StateFlow<StaffManagementState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val staff = staffFirestoreRepository.getAllStaff()
                .filter { it.role != StaffRole.ADMIN.name }
                .sortedWith(compareBy({ it.role }, { it.name }))
            _state.value = _state.value.copy(staffList = staff, isLoading = false)
        }
    }

    fun  showCreateDialog() = _state.update { it.copy(showCreateDialog = true) }
    fun dismissCreateDialog() = _state.update { it.copy(
        showCreateDialog = false, newName = "", newEmail = "", newPassword = "",
        newRole = StaffRole.DOCTOR, newRoomNumber = "", newSpecialization = ""
    ) }
    fun onNameChanged(v: String) = _state.update { it.copy(newName = v) }
    fun onEmailChanged(v: String) = _state.update { it.copy(newEmail = v) }
    fun onPasswordChanged(v: String) = _state.update { it.copy(newPassword = v) }
    fun onRoleChanged(v: StaffRole) = _state.update { it.copy(newRole = v) }
    fun onRoomChanged(v: String) = _state.update { it.copy(newRoomNumber = v) }
    fun onSpecializationChanged(v: String) = _state.update { it.copy(newSpecialization = v) }


    fun createStaff() {
        val s = _state.value
        if (s.newName.isBlank() || s.newEmail.isBlank() || s.newPassword.isBlank()) {
            _state.value = s.copy(error = "Name, email, and password are required")
            return
        }
        if (s.newRole == StaffRole.DOCTOR && s.newRoomNumber.isBlank()) {
            _state.value = s.copy(error = "Room number is required for doctors")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isCreating = true)
            val id = UUID.randomUUID().toString()
            val hash = sha256(s.newPassword)

            val staff = StaffAccount(
                id = id,
                name = s.newName.trim(),
                email = s.newEmail.trim().lowercase(),
                role = s.newRole.name,
                roomNumber = s.newRoomNumber.trim(),
                specialization = s.newSpecialization.trim(),
                isOnDuty = false,
                passwordHash = hash
            )

            // Save to Firestore (for presence tracking + receptionist doctor picker)
            staffFirestoreRepository.upsertStaff(staff)

            // Save to local Room (for desktop login auth)
            adminAccountDao.upsert(
                AdminAccountEntity(
                    id = id,
                    name = s.newName.trim(),
                    email = s.newEmail.trim().lowercase(),
                    passwordHash = hash,
                    role = s.newRole.name
                )
            )

            _state.value = _state.value.copy(
                isCreating = false,
                showCreateDialog = false,
                successMessage = "${s.newRole.name.lowercase().replaceFirstChar { it.uppercase() }} account created",
                newName = "", newEmail = "", newPassword = "", newRoomNumber = "", newSpecialization = ""
            )
            refresh()
        }
    }

    fun deleteStaff(staff: StaffAccount) {
        viewModelScope.launch {
            staffFirestoreRepository.deleteStaff(staff.id)
            // Also remove from local Room
            adminAccountDao.getByEmail(staff.email)?.let {
                adminAccountDao.delete(it)
            }
            refresh()
        }
    }

    fun dismissFeedback() = _state.update { it.copy(error = null, successMessage = null) }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
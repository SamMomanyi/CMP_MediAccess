package org.sammomanyi.mediaccess.features.queue.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole

@Composable
fun StaffManagementScreen(viewModel: StaffManagementViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null || state.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left: staff list ──────────────────────────────
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Staff Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        FilledTonalButton(
                            onClick = viewModel::showCreateDialog,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Staff")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Group by role
                        val grouped = state.staffList.groupBy { it.role }
                        grouped.forEach { (role, members) ->
                            item {
                                Text(
                                    role.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(members) { staff ->
                                StaffListItem(
                                    staff = staff,
                                    onDelete = { viewModel.deleteStaff(staff) }
                                )
                            }
                        }
                        if (state.staffList.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No staff accounts yet.\nTap 'Add Staff' to create one.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // ── Right: info panel ─────────────────────────────
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ManageAccounts,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Staff Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create accounts for doctors, receptionists, and pharmacists.\nEach staff member can log in to their dedicated dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                // Role summary chips
                listOf(
                    Triple("Receptionist", "Cover approvals + check-in verification + queue assignment", Color(0xFF7C3AED)),
                    Triple("Doctor", "Patient queue management", Color(0xFF0891B2)),
                    Triple("Pharmacist", "Billing + prescription (Phase 2)", Color(0xFF059669))
                ).forEach { (role, desc, color) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = color.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = color
                            ) {}
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    role,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Feedback snackbar ─────────────────────────────────
        state.successMessage?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = Color(0xFF10B981)
            ) { Text(msg, color = Color.White) }
        }
        state.error?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) { Text(msg, color = Color.White) }
        }
    }

    // ── Create staff dialog ───────────────────────────────────
    if (state.showCreateDialog) {
        CreateStaffDialog(
            state = state,
            onNameChanged = viewModel::onNameChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onRoleChanged = viewModel::onRoleChanged,
            onRoomChanged = viewModel::onRoomChanged,
            onSpecializationChanged = viewModel::onSpecializationChanged,
            onConfirm = viewModel::createStaff,
            onDismiss = viewModel::dismissCreateDialog
        )
    }
}

@Composable
private fun StaffListItem(staff: StaffAccount, onDelete: () -> Unit) {
    val roleColor = when (staff.role) {
        StaffRole.DOCTOR.name -> Color(0xFF0891B2)
        StaffRole.RECEPTIONIST.name -> Color(0xFF7C3AED)
        StaffRole.PHARMACIST.name -> Color(0xFF059669)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online indicator
            Box {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = roleColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            staff.name.take(2).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = roleColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // On duty indicator
                if (staff.isOnDuty) {
                    Surface(
                        modifier = Modifier.size(10.dp).align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = Color(0xFF10B981)
                    ) {}
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(staff.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(staff.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (staff.roomNumber.isNotBlank()) {
                    Text("Room ${staff.roomNumber}", style = MaterialTheme.typography.labelSmall, color = roleColor)
                }
            }

            // On-duty badge
            if (staff.isOnDuty) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF10B981).copy(alpha = 0.12f)) {
                    Text(
                        "On Duty",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateStaffDialog(
    state: StaffManagementState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRoleChanged: (StaffRole) -> Unit,
    onRoomChanged: (String) -> Unit,
    onSpecializationChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val roles = listOf(StaffRole.RECEPTIONIST, StaffRole.DOCTOR, StaffRole.PHARMACIST)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.newName,
                    onValueChange = onNameChanged,
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.newEmail,
                    onValueChange = onEmailChanged,
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = onPasswordChanged,
                    label = { Text("Password *") },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                    else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                // Role picker
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = state.newRole.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { onRoleChanged(role); roleDropdownExpanded = false }
                            )
                        }
                    }
                }
                if (state.newRole == StaffRole.DOCTOR) {
                    OutlinedTextField(
                        value = state.newRoomNumber,
                        onValueChange = onRoomChanged,
                        label = { Text("Room Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.newSpecialization,
                        onValueChange = onSpecializationChanged,
                        label = { Text("Specialization") },
                        placeholder = { Text("e.g. General Practice") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isCreating,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
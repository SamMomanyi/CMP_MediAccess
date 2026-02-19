package org.sammomanyi.mediaccess.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.pharmacy.presentation.PharmacistDashboardScreen
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import org.sammomanyi.mediaccess.features.queue.presentation.DoctorDashboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen() {
    val viewModel: AdminLoginViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    // Route to the right dashboard based on role
    state.loggedInAccount?.let { account ->
        when (account.role) {
            StaffRole.DOCTOR.name -> {
                DoctorDashboardScreen(
                    account = account,
                    onLogout = { viewModel.logout(account) }
                )
                return
            }
        StaffRole.PHARMACIST.name -> PharmacistDashboardScreen(
            account = TODO(),
            onLogout = TODO()
        )
            else -> {
                AdminDashboard(
                    account = account,
                    onLogout = { viewModel.logout(account) }
                )
                return
            }
        }
    }

    // ── Login form ────────────────────────────────────────────
    var passwordVisible by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    "MediAccess Staff",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sign in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = when (state.selectedRole) {
                            StaffRole.ADMIN -> "Admin"
                            StaffRole.RECEPTIONIST -> "Receptionist"
                            StaffRole.DOCTOR -> "Doctor"
                            StaffRole.PHARMACIST -> "Pharmacist"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Login as") },
                        leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        StaffRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.onRoleSelected(role)
                                    roleDropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        when (role) {
                                            StaffRole.ADMIN -> Icons.Default.AdminPanelSettings
                                            StaffRole.RECEPTIONIST -> Icons.Default.Desk
                                            StaffRole.DOCTOR -> Icons.Default.MedicalServices
                                            StaffRole.PHARMACIST -> Icons.Default.Medication
                                        },
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                state.error?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = viewModel::login,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
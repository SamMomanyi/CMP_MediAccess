package org.sammomanyi.mediaccess.features.identity.presentation.personal

import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun PersonalScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    viewModel: PersonalViewModel = koinViewModel { parametersOf(onLogout) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MediAccessColors.Background)
            .padding(padding)
    ) {
        // Header
        item {
            PersonalHeader()
        }

        // Profile Section
        item {
            ProfileSection(
                user = state.user,
                onEditProfile = { viewModel.onAction(PersonalAction.OnEditProfile) }
            )
        }

        // Recovery Emails
        item {
            RecoveryEmailsSection(
                emails = state.recoveryEmails,
                onAddEmail = { viewModel.onAction(PersonalAction.OnAddRecoveryEmail) }
            )
        }

        // Recovery Phones
        item {
            RecoveryPhonesSection(
                phones = state.recoveryPhones,
                onAddPhone = { viewModel.onAction(PersonalAction.OnAddRecoveryPhone) }
            )
        }

        // My Topics
        item {
            MyTopicsSection(
                selectedTopics = state.selectedTopics,
                onConfirm = { viewModel.onAction(PersonalAction.OnConfirmTopics) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Floating Menu Button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding() + 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { /* Menu */ },
            containerColor = MediAccessColors.Secondary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PersonalHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MediAccessColors.Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SA",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart\nACCESS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MediAccessColors.Secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
                IconButton(onClick = { }) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MediAccessColors.SurfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    user: org.sammomanyi.mediaccess.features.identity.domain.model.User?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MediAccessColors.SurfaceVariant
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.padding(20.dp),
                    tint = MediAccessColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.fullName?.uppercase() ?: "USER NAME",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MediAccessColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user?.email ?: "email@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MediAccessColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecoveryEmailsSection(
    emails: List<String>,
    onAddEmail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recovery Emails",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddEmail,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Emails", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (emails.isEmpty()) {
                Text(
                    text = "No email found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary
                )
            } else {
                emails.forEach { email ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MediAccessColors.SurfaceVariant
                    ) {
                        Text(
                            text = email,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryPhonesSection(
    phones: List<String>,
    onAddPhone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recovery Phones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddPhone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Phones", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No phone found",
                style = MaterialTheme.typography.bodyMedium,
                color = MediAccessColors.TextSecondary
            )
        }
    }
}

@Composable
private fun MyTopicsSection(
    selectedTopics: List<String>,
    onConfirm: () -> Unit
) {
    val allTopics = listOf(
        "Cancer", "Diabetes", "Hypertension",
        "Mental Health", "Ear", "Smoking",
        "Flu", "Hair", "Alcohol"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "My Topics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Topics Grid using FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTopics.forEach { topic ->
                    val isSelected = selectedTopics.contains(topic)
                    FilterChip(
                        selected = isSelected,
                        onClick = { /* Toggle topic */ },
                        label = {
                            Text(
                                topic,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MediAccessColors.Secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Confirm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
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
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueEntry
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueStatus

@Composable
fun DoctorDashboardScreen(
    account: AdminAccountEntity,
    onLogout: () -> Unit,
    viewModel: DoctorQueueViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.error) {
        if (state.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissError()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
// In DoctorDashboardScreen, inside the top bar Row:
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFF0891B2).copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF0891B2))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Dr. ${account.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Doctor Dashboard · ${account.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // ✅ NEW: Availability Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "Available",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isAvailable) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = state.isAvailable,
                            onCheckedChange = { viewModel.toggleAvailability() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            )
                        )
                    }

                    state.lastRefreshedAt?.let { ts ->
                        val time = kotlinx.datetime.Instant.fromEpochMilliseconds(ts)
                            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        Text(
                            "Updated ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                    OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sign Out")
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left panel: queue ─────────────────────────────
            Column(
                modifier = Modifier.width(420.dp).fillMaxHeight().padding(16.dp)
            ) {
                // Current patient card
                Text("Current Patient", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                if (state.currentPatient != null) {
                    CurrentPatientCard(
                        patient = state.currentPatient!!,
                        isActionInProgress = state.actionInProgress,
                        onMarkDone = viewModel::markDone
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (state.waitingQueue.isEmpty()) "No patients in queue"
                                else "Tap a patient below to call them",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Waiting queue
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Waiting Queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    if (state.waitingQueue.isNotEmpty()) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                            Text(
                                "${state.waitingQueue.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (state.waitingQueue.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No patients waiting", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.waitingQueue) { entry ->
                            WaitingPatientCard(
                                entry = entry,
                                onCall = { viewModel.callPatient(entry.id) },
                                isActionInProgress = state.actionInProgress
                            )
                        }
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // ── Right panel: completed today ──────────────────
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Completed Today", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${state.completedToday.size} patients",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981)
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (state.completedToday.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("No consultations completed yet today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.completedToday) { entry ->
                            CompletedPatientCard(entry = entry)
                        }
                    }
                }
            }
        }

        // Snackbar
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) { Text(error, color = Color.White) }
        }
    }
}

@Composable
private fun CurrentPatientCard(
    patient: QueueEntry,
    isActionInProgress: Boolean,
    onMarkDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0891B2).copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0891B2).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = Color(0xFF10B981)) {}
                Spacer(Modifier.width(6.dp))
                Text("In Consultation", style = MaterialTheme.typography.labelMedium, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(patient.patientName.ifBlank { patient.patientEmail.substringBefore("@") }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(patient.patientEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(label = patient.purpose)
                InfoChip(label = patient.insuranceName)
                InfoChip(label = "No. ${patient.memberNumber}")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onMarkDone,
                enabled = !isActionInProgress,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("✓ Done — Call Next Patient", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WaitingPatientCard(
    entry: QueueEntry,
    onCall: () -> Unit,
    isActionInProgress: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position badge
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "#${entry.queuePosition}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.patientName.ifBlank { entry.patientEmail.substringBefore("@") }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${entry.purpose} · ${entry.insuranceName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(
                onClick = onCall,
                enabled = !isActionInProgress,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Call", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CompletedPatientCard(entry: QueueEntry) {
    val completedTime = entry.completedAt?.let { ts ->
        val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(ts)
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } ?: "--:--"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.patientName.ifBlank { entry.patientEmail.substringBefore("@") }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(entry.purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(completedTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
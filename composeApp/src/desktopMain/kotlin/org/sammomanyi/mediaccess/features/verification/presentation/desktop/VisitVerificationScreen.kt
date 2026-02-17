package org.sammomanyi.mediaccess.features.verification.presentation.desktop

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
import org.sammomanyi.mediaccess.features.verification.data.desktop.CoverVerificationStatus

@Composable
fun VisitVerificationScreen(viewModel: VisitVerificationViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {

        // ── Left panel: verification + assignment ─────────────
        Column(
            modifier = Modifier.width(420.dp).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Check-In Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Code entry card
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.codeInput,
                        onValueChange = viewModel::onCodeInputChanged,
                        label = { Text("Enter Check-In Code") },
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        isError = state.verifyError != null
                    )

                    state.verifyError?.let { error ->
                        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    if (state.assignSuccess) {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFF10B981).copy(alpha = 0.12f)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Patient added to queue!", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Button(
                            onClick = viewModel::resetForNextPatient,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Next Patient") }
                    } else {
                        Button(
                            onClick = viewModel::verifyCode,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = state.codeInput.isNotBlank() && !state.isVerifying && !state.showDoctorPicker,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text("Verify Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Verified result card
            state.verifiedResult?.let { result ->
                if (result.coverStatus == CoverVerificationStatus.APPROVED || state.showDoctorPicker || state.assignSuccess) {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.12f)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Verified", style = MaterialTheme.typography.labelMedium, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                    Text(result.patientEmail, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            HorizontalDivider()
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem("Purpose", result.purpose)
                                InfoItem("Insurance", result.insuranceName)
                                InfoItem("Member No.", result.memberNumber)
                            }
                        }
                    }
                }
            }

            // Doctor picker (shown after verification)
            if (state.showDoctorPicker && !state.assignSuccess) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Assign to Doctor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        if (state.isLoadingDoctors) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else if (state.onDutyDoctors.isEmpty()) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                                Text("No doctors currently on duty.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                            }
                        } else {
                            state.onDutyDoctors.forEach { doctor ->
                                DoctorSelectCard(
                                    doctor = doctor,
                                    isSelected = state.selectedDoctor?.id == doctor.id,
                                    onSelect = { viewModel.selectDoctor(doctor) }
                                )
                            }
                        }

                        state.assignError?.let { error ->
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }

                        Button(
                            onClick = viewModel::confirmAssignment,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = state.selectedDoctor != null && !state.isAssigning,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isAssigning) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Confirm & Add to Queue", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        // ── Right panel: today's history ──────────────────────
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
            Text("Today's Check-Ins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (state.todayHistory.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        Text("No check-ins yet today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.todayHistory) { entry ->
                        HistoryEntryCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorSelectCard(doctor: StaffAccount, isSelected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (isSelected) Color(0xFF0891B2) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (isSelected) Color(0xFF0891B2).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFF0891B2).copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(doctor.name.take(2).uppercase(), style = MaterialTheme.typography.labelMedium, color = Color(0xFF0891B2), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dr. ${doctor.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Room ${doctor.roomNumber}${if (doctor.specialization.isNotBlank()) " · ${doctor.specialization}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0891B2), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: VerificationHistoryEntry) {
    val time = kotlinx.datetime.Instant.fromEpochMilliseconds(entry.usedAt)
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val timeStr = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.patientEmail, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${entry.purpose} · ${entry.insuranceName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
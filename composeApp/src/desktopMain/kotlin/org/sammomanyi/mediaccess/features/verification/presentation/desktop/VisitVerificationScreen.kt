package org.sammomanyi.mediaccess.features.verification.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.verification.data.desktop.CoverVerificationStatus
import org.sammomanyi.mediaccess.features.verification.data.desktop.VerifiedVisitResult

@Composable
fun VisitVerificationScreen() {
    val viewModel: VisitVerificationViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {

        // ── Left: code entry + result ─────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Visit Verification",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Enter the code shown on the patient's phone. " +
                        "Verification requires an approved insurance cover.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CodeEntryCard(
                code      = state.codeInput,
                isLoading = state.isVerifying,
                onChange  = viewModel::onCodeInputChanged,
                onVerify  = viewModel::verifyCode
            )

            if (state.verificationError != null) {
                ErrorBanner(message = state.verificationError!!)
            }

            if (state.markUsedSuccess) {
                SuccessBanner(onDismiss = viewModel::clearResult)
            }

            if (state.verificationResult != null) {
                VerificationResultCard(
                    result        = state.verificationResult!!,
                    isMarkingUsed = state.isMarkingUsed,
                    onMarkAsUsed  = viewModel::markAsUsed,
                    onClear       = viewModel::clearResult
                )
            }
        }

        VerticalDivider()

        // ── Right: today's history ────────────────────────────
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today's Verifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (state.todayHistory.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${state.todayHistory.size}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            HorizontalDivider()

            if (state.todayHistory.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No verifications yet today",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(state.todayHistory) { entry ->
                        HistoryItem(entry = entry)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ── Code entry card ───────────────────────────────────────────

@Composable
private fun CodeEntryCard(
    code: String,
    isLoading: Boolean,
    onChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Enter Visit Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = code,
                onValueChange = onChange,
                label = { Text("Visit Code") },
                placeholder = { Text("e.g. MED-4821") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onVerify() }),
                enabled = !isLoading
            )
            Button(
                onClick = onVerify,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = code.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, null, modifier = Modifier.padding(end = 8.dp))
                    Text("Verify Code")
                }
            }
        }
    }
}

// ── Verification result card ──────────────────────────────────

@Composable
private fun VerificationResultCard(
    result: VerifiedVisitResult,
    isMarkingUsed: Boolean,
    onMarkAsUsed: () -> Unit,
    onClear: () -> Unit
) {
    // Cover is approved — all lights green
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Approved banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            "✓ Cover Verified — Proceed",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            result.insuranceName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            HorizontalDivider()

            DetailSection(title = "Patient Details") {
                DetailRow("Name",       result.patientName)
                DetailRow("Email",      result.patientEmail)
                DetailRow("Medical ID", result.medicalId)
            }

            HorizontalDivider()

            DetailSection(title = "Visit Details") {
                DetailRow("Purpose",  result.purpose.lowercase().replaceFirstChar { it.uppercase() })
                DetailRow("Expires",  formatTimestamp(result.expiresAt))
            }

            HorizontalDivider()

            DetailSection(title = "Insurance Details") {
                result.insuranceName?.let { DetailRow("Insurer",   it) }
                result.memberNumber?.let  { DetailRow("Member No", it) }
                result.country?.let       { DetailRow("Country",   it) }
            }

            HorizontalDivider()

            // What is covered for this purpose
            CoverageInfoBox(purpose = result.purpose)

            HorizontalDivider()

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }

                Button(
                    onClick = onMarkAsUsed,
                    enabled = !isMarkingUsed,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    if (isMarkingUsed) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Mark as Used")
                    }
                }
            }
        }
    }
}

// ── Coverage info ─────────────────────────────────────────────

@Composable
private fun CoverageInfoBox(purpose: String) {
    val (icon, title, description) = when (purpose.uppercase()) {
        "CONSULTATION" -> Triple(
            Icons.Default.MedicalServices,
            "Consultation Visit",
            "Patient's insurer covers consultation fees. Proceed with the appointment."
        )
        "PHARMACY" -> Triple(
            Icons.Default.LocalPharmacy,
            "Pharmacy Dispensing",
            "Patient's insurer covers medication. Verify prescription before dispensing."
        )
        else -> Triple(
            Icons.Default.Info,
            "General Visit",
            "Confirm which services are covered with the patient's insurer."
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon, null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(title, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

// ── History item ──────────────────────────────────────────────

@Composable
private fun HistoryItem(entry: VerificationHistoryEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.patientName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${entry.purpose.lowercase().replaceFirstChar { it.uppercase() }} · " +
                        formatTime(entry.verifiedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Banners ───────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.ErrorOutline, null,
                tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun SuccessBanner(onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, null,
                tint = MaterialTheme.colorScheme.primary)
            Text(
                "Visit verified and code marked as used.",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) { Text("New") }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatTimestamp(ms: Long): String {
    val local = Instant.fromEpochMilliseconds(ms)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%d %s %d, %02d:%02d".format(
        local.dayOfMonth,
        local.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3),
        local.year, local.hour, local.minute
    )
}

private fun formatTime(ms: Long): String {
    val local = Instant.fromEpochMilliseconds(ms)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(local.hour, local.minute)
}
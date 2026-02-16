package org.sammomanyi.mediaccess

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sammomanyi.mediaccess.features.admin.domain.model.Admin
import org.sammomanyi.mediaccess.features.cover.presentation.AdminCoverScreen
import org.sammomanyi.mediaccess.features.verification.presentation.desktop.VisitVerificationScreen

private enum class DashboardSection(val label: String, val icon: ImageVector) {
    COVER_REQUESTS("Cover Requests", Icons.Default.AssignmentTurnedIn),
    VISIT_VERIFICATION("Visit Verification", Icons.Default.QrCodeScanner)
}

@Composable
fun AdminDashboard(
    admin: Admin,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(DashboardSection.COVER_REQUESTS) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "MediAccess Admin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Signed in as ${admin.name} · ${admin.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onLogout) {
                        Text("Sign Out")
                    }
                }

                // Section tabs
                TabRow(selectedTabIndex = selectedSection.ordinal) {
                    DashboardSection.entries.forEach { section ->
                        Tab(
                            selected  = selectedSection == section,
                            onClick   = { selectedSection = section },
                            icon      = {
                                Icon(section.icon, null, modifier = Modifier.size(18.dp))
                            },
                            text      = { Text(section.label) }
                        )
                    }
                }
            }
        }

        // ── Tab content ───────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedSection) {
                DashboardSection.COVER_REQUESTS     -> AdminCoverScreen()
                DashboardSection.VISIT_VERIFICATION -> VisitVerificationScreen()
            }
        }
    }
}
package org.sammomanyi.mediaccess.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.cover.presentation.AdminCoverScreen
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import org.sammomanyi.mediaccess.features.queue.presentation.StaffManagementScreen
import org.sammomanyi.mediaccess.features.verification.presentation.desktop.VisitVerificationScreen

@Composable
fun AdminDashboard(
    account: AdminAccountEntity,
    onLogout: () -> Unit
) {
    val isAdmin = account.role == StaffRole.ADMIN.name

    // Build tabs based on role
    // RECEPTIONIST: Cover Requests + Check-In Verification
    // ADMIN: Cover Requests + Check-In Verification + Staff Management
    val tabs = buildList {
        add(Triple("Cover Requests", Icons.Default.AssignmentTurnedIn, 0))
        add(Triple("Check-In Verification", Icons.Default.QrCodeScanner, 1))
        if (isAdmin) add(Triple("Staff Management", Icons.Default.ManageAccounts, 2))
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top bar ───────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "MediAccess Admin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Signed in as ${account.name} · ${account.role} · ${account.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onLogout, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sign Out")
                    }
                }

                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { idx, (label, icon, _) ->
                        Tab(
                            selected = selectedIndex == idx,
                            onClick = { selectedIndex = idx },
                            text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }

        // ── Content ───────────────────────────────────────────
        when (tabs[selectedIndex].third) {
            0 -> AdminCoverScreen()
            1 -> VisitVerificationScreen()
            2 -> StaffManagementScreen()
        }
    }
}
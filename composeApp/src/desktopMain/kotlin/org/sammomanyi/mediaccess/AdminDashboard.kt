package org.sammomanyi.mediaccess

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
    account: AdminAccountEntity,  // ✅ Changed from Admin to AdminAccountEntity
    onLogout: () -> Unit
) {
    // ✅ ADMIN: Only Staff Management
    // ✅ RECEPTIONIST: Cover Requests + Check-In Verification
    val tabs = buildList {
        if (account.role == StaffRole.ADMIN.name) {
            add(Triple("Staff Management", Icons.Default.ManageAccounts, 0))
        } else if (account.role == StaffRole.RECEPTIONIST.name) {
            add(Triple("Cover Requests", Icons.Default.AssignmentTurnedIn, 0))
            add(Triple("Check-In Verification", Icons.Default.QrCodeScanner, 1))
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
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
                            "MediAccess ${account.role.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Signed in as ${account.name} · ${account.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onLogout,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sign Out")
                    }
                }

                if (tabs.isNotEmpty()) {
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
        }

        // Content based on role
        if (tabs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No permissions assigned", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            when {
                account.role == StaffRole.ADMIN.name -> {
                    // Admin only sees Staff Management
                    StaffManagementScreen()
                }
                account.role == StaffRole.RECEPTIONIST.name -> {
                    // Receptionist sees Cover + Verification
                    when (selectedIndex) {
                        0 -> AdminCoverScreen()
                        1 -> VisitVerificationScreen()
                    }
                }
            }
        }
    }
}
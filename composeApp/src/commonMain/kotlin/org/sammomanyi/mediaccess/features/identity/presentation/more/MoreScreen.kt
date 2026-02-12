package org.sammomanyi.mediaccess.features.identity.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.core.presentation.theme.ThemeViewModel

@Composable
fun MoreScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel = koinViewModel()
) {

    val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
    var biometricsEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MediAccessColors.Background)
            .padding(padding)
    ) {
        // Header
        item {
            MoreHeader()
        }

        // Theme Toggle
        item { SectionHeader("Appearance") }

        item {
            ThemeToggle(
                isDarkMode = isDarkMode,
                onToggle = { themeViewModel.toggleTheme() }
            )
        }
        // Biometrics
        item {
            BiometricsToggle(
                enabled = biometricsEnabled,
                onToggle = { biometricsEnabled = it }
            )
        }

        // Share
        item {
            SectionHeader("Share")
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Share,
                title = "Tell a Friend",
                onClick = { }
            )
        }

        // Rate
        item {
            SectionHeader("Rate")
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Star,
                title = "Rate Us",
                onClick = { }
            )
        }

        // Help
        item {
            SectionHeader("Help")
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Help,
                title = "Support",
                onClick = { }
            )
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.QuestionAnswer,
                title = "Frequently Asked Questions",
                onClick = { }
            )
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Phone,
                title = "Contact Us",
                onClick = { }
            )
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Search,
                title = "About Us",
                onClick = { }
            )
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Shield,
                title = "T&C And Privacy Policy",
                onClick = { }
            )
        }

        // Version
        item {
            SectionHeader("Version")
        }
        item {
            MoreMenuItem(
                icon = Icons.Default.Tag,
                title = "APP Version",
                onClick = { }
            )
        }

        // Logout
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MoreHeader() {
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
                            text = "MA",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MediAccess",
                    style = MaterialTheme.typography.titleMedium,
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
private fun BiometricsToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
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
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MediAccessColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Biometrics Login",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MediAccessColors.TextPrimary
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MediAccessColors.Secondary,
                    checkedTrackColor = MediAccessColors.Secondary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MediAccessColors.SurfaceVariant
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MediAccessColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MediAccessColors.TextPrimary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MediAccessColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MediAccessColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ThemeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isDarkMode) "Dark Mode" else "Light Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Switch to light theme" else "Switch to dark theme",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
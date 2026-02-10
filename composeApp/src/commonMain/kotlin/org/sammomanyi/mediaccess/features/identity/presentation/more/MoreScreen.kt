package org.sammomanyi.mediaccess.features.identity.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun MoreScreen(padding: PaddingValues) {
    var biometricsEnabled by remember { mutableStateOf(false) }

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

        // Biometrics Login
        item {
            BiometricsToggle(
                enabled = biometricsEnabled,
                onToggle = { biometricsEnabled = it }
            )
        }

        // Share Section
        item {
            SectionHeader("Share")
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Share,
                title = "Tell a Friend",
                onClick = { /* Share */ }
            )
        }

        // Rate Section
        item {
            SectionHeader("Rate")
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Star,
                title = "Rate Us",
                onClick = { /* Rate */ }
            )
        }

        // My Dawa Section
        item {
            SectionHeader("My Dawa")
        }

        item {
            MoreMenuItem(
                icon = null,
                title = "Register to Mzima Program",
                iconPlaceholder = "MZIMA",
                onClick = { /* Mzima */ }
            )
        }

        item {
            MoreMenuItem(
                icon = null,
                title = "About Mzima Program",
                iconPlaceholder = "MZIMA",
                onClick = { /* About Mzima */ }
            )
        }

        // Help Section
        item {
            SectionHeader("Help")
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Help,
                title = "Support",
                onClick = { /* Support */ }
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.QuestionAnswer,
                title = "Frequently Asked Questions",
                onClick = { /* FAQ */ }
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Phone,
                title = "Contact Us",
                onClick = { /* Contact */ }
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Search,
                title = "About Us",
                onClick = { /* About */ }
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Shield,
                title = "T&C And Privacy Policy",
                onClick = { /* T&C */ }
            )
        }

        // Version Section
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    icon: ImageVector?,
    title: String,
    iconPlaceholder: String? = null,
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
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MediAccessColors.TextPrimary
                )
            } else if (iconPlaceholder != null) {
                // Placeholder for Mzima logo
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MediAccessColors.Secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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
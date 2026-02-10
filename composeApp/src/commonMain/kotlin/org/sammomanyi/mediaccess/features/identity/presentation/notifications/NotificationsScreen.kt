package org.sammomanyi.mediaccess.features.identity.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NotificationTab.ALL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "My Notifications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MediAccessColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == NotificationTab.ALL,
                onClick = { selectedTab = NotificationTab.ALL },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MediAccessColors.TextSecondary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            FilterChip(
                selected = selectedTab == NotificationTab.UNREAD,
                onClick = { selectedTab = NotificationTab.UNREAD },
                label = { Text("Unread") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MediAccessColors.TextSecondary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            FilterChip(
                selected = selectedTab == NotificationTab.MY_NOTIFICATIONS,
                onClick = { selectedTab = NotificationTab.MY_NOTIFICATIONS },
                label = { Text("My Notifications") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MediAccessColors.TextSecondary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { /* Mark all as read */ }) {
                Text(
                    "Mark all as read",
                    color = MediAccessColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empty State
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notifications",
                style = MaterialTheme.typography.bodyLarge,
                color = MediAccessColors.TextSecondary
            )
        }
    }
}

enum class NotificationTab {
    ALL,
    UNREAD,
    MY_NOTIFICATIONS
}
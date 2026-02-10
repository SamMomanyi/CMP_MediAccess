package org.sammomanyi.mediaccess.features.identity.presentation.cover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun CoverScreen(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediAccessColors.Background)
            .padding(padding)
    ) {
        // Header
        CoverHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Empty State
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustration placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = MediAccessColors.Secondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MediAccessColors.Secondary
                )
                // "No data found" text overlay
                Text(
                    text = "No data\nfound",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This account has no cover linked to it",
                style = MaterialTheme.typography.bodyLarge,
                color = MediAccessColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Kindly Click on the ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary
                )
                Text(
                    text = "Link Cover",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " to add a cover.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { /* Link Cover */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Link Cover",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { /* Cover Status */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Cover Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Floating Action Button (Menu)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
}

@Composable
private fun CoverHeader() {
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
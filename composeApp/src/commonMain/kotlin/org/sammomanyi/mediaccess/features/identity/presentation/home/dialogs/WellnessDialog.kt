package org.sammomanyi.mediaccess.features.identity.presentation.home.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun WellnessDialog(
    onDismiss: () -> Unit
) {
    var agreedToTerms by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MediAccessColors.Secondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "to Smart Fitness",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MediAccessColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MediAccessColors.SurfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MediAccessColors.Primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "Stay on top of your wellness goals with smart fitness, simple tool designed to keep you feeling your best. Track your steps, set hydration reminders, and build healthy habits all in one place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // Terms Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MediAccessColors.Primary
                        )
                    )
                    Text(
                        text = "I have agreed to the ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Privacy policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " and ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Terms of use",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = { /* Enable wellness */ },
                    enabled = agreedToTerms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.TextSecondary,
                        disabledContainerColor = MediAccessColors.SurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "LETS DO IT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Powered by ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextSecondary
                    )
                    Text(
                        text = "MediAccess",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
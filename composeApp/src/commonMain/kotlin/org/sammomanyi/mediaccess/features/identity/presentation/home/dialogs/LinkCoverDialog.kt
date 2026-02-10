package org.sammomanyi.mediaccess.features.identity.presentation.home.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCoverDialog(
    userEmail: String,
    onDismiss: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf("KENYA") }
    var email by remember { mutableStateOf(userEmail) }
    var isAutoLinkage by remember { mutableStateOf(true) }

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
                    .padding(24.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Setup Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MediAccessColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MediAccessColors.Primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "1",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "automatic linkage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MediAccessColors.Primary
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        color = MediAccessColors.SurfaceVariant
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MediAccessColors.SurfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "2",
                                    color = MediAccessColors.TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "manual linkage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MediAccessColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Country Dropdown
                Text(
                    text = "Country : *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expandedCountry by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedCountry,
                    onExpandedChange = { expandedCountry = !expandedCountry }
                ) {
                    OutlinedTextField(
                        value = selectedCountry,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountry)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCountry,
                        onDismissRequest = { expandedCountry = false }
                    ) {
                        listOf("KENYA", "UGANDA", "TANZANIA", "RWANDA").forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country) },
                                onClick = {
                                    selectedCountry = country
                                    expandedCountry = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Email
                Text(
                    text = "Email :",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MediAccessColors.Secondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        focusedContainerColor = MediAccessColors.PrimaryLight.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.weight(1f))

                // Link Button
                Button(
                    onClick = { /* Link account */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediAccessColors.Secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Link Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
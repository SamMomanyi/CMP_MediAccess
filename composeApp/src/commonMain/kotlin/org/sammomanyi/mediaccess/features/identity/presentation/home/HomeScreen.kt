package org.sammomanyi.mediaccess.features.identity.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.asString
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode

@Composable
fun HomeScreen(
    padding: PaddingValues,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Section
        WelcomeCard(user = state.user)

        Spacer(modifier = Modifier.height(24.dp))

        // Visit Code Section
        VisitCodeCard(
            activeVisitCode = state.activeVisitCode,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onGenerateClick = { viewModel.onAction(HomeAction.OnGenerateVisitCode) },
            onRefreshClick = { viewModel.onAction(HomeAction.OnRefreshVisitCode) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        QuickActionsSection()
    }
}

@Composable
private fun WelcomeCard(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Welcome, ${user?.firstName ?: "User"}!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Medical ID: ${user?.medicalId ?: "Loading..."}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun VisitCodeCard(
    activeVisitCode: VisitCode?,
    isLoading: Boolean,
    errorMessage: org.sammomanyi.mediaccess.core.presentation.UiText?,
    onGenerateClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "Visit Code",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hospital Visit Code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activeVisitCode != null && activeVisitCode.isValid()) {
                ActiveCodeDisplay(
                    visitCode = activeVisitCode,
                    onRefreshClick = onRefreshClick
                )
            } else {
                NoCodeDisplay(
                    isLoading = isLoading,
                    onGenerateClick = onGenerateClick
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActiveCodeDisplay(
    visitCode: VisitCode,
    onRefreshClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = visitCode.code,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            val timeRemaining = visitCode.timeRemaining()
            val minutes = timeRemaining / 60
            val seconds = timeRemaining % 60

            Text(
                text = "Expires in: ${minutes}m ${seconds}s",
                style = MaterialTheme.typography.bodySmall,
                color = if (minutes < 5) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Purpose: ${visitCode.purpose.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = onRefreshClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Generate New Code")
    }
}

@Composable
private fun NoCodeDisplay(
    isLoading: Boolean,
    onGenerateClick: () -> Unit
) {
    Text(
        text = "No active visit code",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onGenerateClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Generate Visit Code")
        }
    }
}

@Composable
private fun QuickActionsSection() {
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCardItem(
            title = "View Records",
            modifier = Modifier.weight(1f),
            onClick = { /* Handled by bottom nav */ }
        )
        QuickActionCardItem(
            title = "Find Hospital",
            modifier = Modifier.weight(1f),
            onClick = { /* Handled by bottom nav */ }
        )
    }
}

// Renamed to avoid conflicts
@Composable
private fun QuickActionCardItem(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
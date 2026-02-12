package org.sammomanyi.mediaccess.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Use these instead of hardcoded colors throughout the app
object AppColors {
    // These use MaterialTheme so they auto-switch with dark/light mode
    val cardBackground @Composable get() = MaterialTheme.colorScheme.surface
    val screenBackground @Composable get() = MaterialTheme.colorScheme.background
    val textPrimary @Composable get() = MaterialTheme.colorScheme.onSurface
    val textSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val divider @Composable get() = MaterialTheme.colorScheme.outline
    val icon @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val inputBackground @Composable get() = MaterialTheme.colorScheme.surfaceVariant
}
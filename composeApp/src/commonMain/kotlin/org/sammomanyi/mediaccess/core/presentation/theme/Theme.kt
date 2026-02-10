package org.sammomanyi.mediaccess.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Medical/Health Color Palette
object MediAccessColors {
    // Primary - Teal/Medical Blue
    val Primary = Color(0xFF00838F) // Deep teal
    val PrimaryVariant = Color(0xFF005662)
    val PrimaryLight = Color(0xFF4FB3BF)

    // Secondary - Warm accent
    val Secondary = Color(0xFFFF6B6B) // Coral red for accents
    val SecondaryVariant = Color(0xFFEE5A52)
    val SecondaryLight = Color(0xFFFF9E9E)

    // Background
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE8EDF2)

    // Text
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFF9CA3AF)

    // Success/Error
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
    val Info = Color(0xFF3B82F6)
}

val LightColorScheme = lightColorScheme(
    primary = MediAccessColors.Primary,
    onPrimary = Color.White,
    primaryContainer = MediAccessColors.PrimaryLight,
    onPrimaryContainer = MediAccessColors.PrimaryVariant,

    secondary = MediAccessColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = MediAccessColors.SecondaryLight,
    onSecondaryContainer = MediAccessColors.SecondaryVariant,

    background = MediAccessColors.Background,
    onBackground = MediAccessColors.TextPrimary,

    surface = MediAccessColors.Surface,
    onSurface = MediAccessColors.TextPrimary,
    surfaceVariant = MediAccessColors.SurfaceVariant,
    onSurfaceVariant = MediAccessColors.TextSecondary,

    error = MediAccessColors.Error,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = MediAccessColors.PrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = MediAccessColors.Primary,
    onPrimaryContainer = Color.White,

    secondary = MediAccessColors.SecondaryLight,
    onSecondary = Color.Black,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)
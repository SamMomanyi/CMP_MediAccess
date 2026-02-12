package org.sammomanyi.mediaccess.core.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object MediAccessColors {
    // Primary - Teal/Medical
    val Primary = Color(0xFF00838F)
    val PrimaryVariant = Color(0xFF005662)
    val PrimaryLight = Color(0xFF4FB3BF)

    // Secondary - Coral Red
    val Secondary = Color(0xFFB71C1C)
    val SecondaryVariant = Color(0xFF7F0000)
    val SecondaryLight = Color(0xFFE57373)

    // Light Mode
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE8EDF2)

    // Dark Mode Backgrounds
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkSurfaceVariant = Color(0xFF2C2C2C)
    val DarkSurfaceElevated = Color(0xFF272727)

    // Text - Light
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFF9CA3AF)

    // Text - Dark
    val DarkTextPrimary = Color(0xFFECECEC)
    val DarkTextSecondary = Color(0xFFAAAAAA)

    // Status
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
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
    onError = Color.White,

    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0)
)

val DarkColorScheme = darkColorScheme(
    primary = MediAccessColors.PrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = MediAccessColors.Primary,
    onPrimaryContainer = Color.White,

    secondary = MediAccessColors.SecondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = MediAccessColors.Secondary,
    onSecondaryContainer = Color.White,

    background = MediAccessColors.DarkBackground,
    onBackground = MediAccessColors.DarkTextPrimary,

    surface = MediAccessColors.DarkSurface,
    onSurface = MediAccessColors.DarkTextPrimary,
    surfaceVariant = MediAccessColors.DarkSurfaceVariant,
    onSurfaceVariant = MediAccessColors.DarkTextSecondary,

    error = MediAccessColors.Error,
    onError = Color.White,

    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A)
)
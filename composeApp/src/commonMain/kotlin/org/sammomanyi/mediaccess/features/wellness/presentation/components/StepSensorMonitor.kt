// commonMain/.../components/StepSensorMonitor.kt
package org.sammomanyi.mediaccess.features.wellness.presentation.components

import androidx.compose.runtime.Composable

data class StepSensorState(
    val isSupported: Boolean,
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

@Composable
expect fun rememberStepSensorMonitor(
    onStepsUpdated: (Int) -> Unit
): StepSensorState
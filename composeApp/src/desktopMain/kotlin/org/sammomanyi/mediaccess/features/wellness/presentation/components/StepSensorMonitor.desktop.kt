package org.sammomanyi.mediaccess.features.wellness.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberStepSensorMonitor(
    onStepsUpdated: (Int) -> Unit
): StepSensorState {
    // Desktop has no step sensor - return stub
    return StepSensorState(
        isSupported = false,
        hasPermission = false,
        requestPermission = {}
    )
}
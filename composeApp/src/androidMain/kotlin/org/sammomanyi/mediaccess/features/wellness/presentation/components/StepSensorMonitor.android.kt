// androidMain/.../components/StepSensorMonitor.android.kt
package org.sammomanyi.mediaccess.features.wellness.presentation.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberStepSensorMonitor(
    onStepsUpdated: (Int) -> Unit
): StepSensorState {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    // Permission State
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Shared Preferences to store the "Zero Point"
    val prefs = remember { context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE) }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // 2. The Updated DisposableEffect
    DisposableEffect(hasPermission, stepSensor) {
        if (hasPermission && stepSensor != null) {
            val listener = object : SensorEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.values?.firstOrNull()?.let { rawSteps ->
                        val currentRaw = rawSteps.toInt()

                        // Get today's date to check if we need a reset
                        val todayKey = java.time.LocalDate.now().toString()
                        val savedDate = prefs.getString("last_step_date", "")

                        // LOGIC: If the date has changed, the current sensor value becomes our new "zero"
                        if (savedDate != todayKey) {
                            prefs.edit()
                                .putString("last_step_date", todayKey)
                                .putInt("step_baseline", currentRaw)
                                .apply()
                        }

                        // MATH: Live Steps = Total Since Reboot - Baseline (Steps at start of day)
                        val baseline = prefs.getInt("step_baseline", currentRaw)
                        val todaysSteps = (currentRaw - baseline).coerceAtLeast(0)

                        onStepsUpdated(todaysSteps)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        } else {
            onDispose { }
        }
    }

    return StepSensorState(
        isSupported = stepSensor != null,
        hasPermission = hasPermission,
        requestPermission = {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    )
}
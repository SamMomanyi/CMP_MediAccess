package org.sammomanyi.mediaccess.core.util

import android.Manifest // FIXED: No more java.util.jar.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

actual class LocationHelper(
    private val context: Context,
    private val launcher: () -> Unit
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    actual fun requestPermission() {
        launcher()
    }

    actual fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual fun getCurrentLocation(onResult: (Double?, Double?, String?) -> Unit) {
        try {
            val token = CancellationTokenSource().token
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token)
                .addOnSuccessListener { location ->
                    if (location != null) onResult(location.latitude, location.longitude, null)
                    else onResult(null, null, "Location is null")
                }
                .addOnFailureListener { onResult(null, null, it.message) }
        } catch (e: SecurityException) {
            onResult(null, null, "Permission denied")
        }
    }
}

@Composable
actual fun rememberLocationHelper(onPermissionResult: (Boolean) -> Unit): LocationHelper {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        onPermissionResult(granted)
    }

    return remember {
        LocationHelper(context) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}
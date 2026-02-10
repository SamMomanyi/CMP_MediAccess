package org.sammomanyi.mediaccess.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.jar.Manifest

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false
)

@Composable
fun rememberLocationState(): LocationPermissionState {
    val context = LocalContext.current
    var locationState by remember { mutableStateOf(LocationState()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            locationState = locationState.copy(permissionGranted = true)
        } else {
            locationState = locationState.copy(
                permissionGranted = false,
                error = "Location permission denied"
            )
        }
    }

    return remember {
        LocationPermissionState(
            state = locationState,
            onStateChange = { locationState = it },
            requestPermission = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            checkPermission = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                locationState = locationState.copy(permissionGranted = hasPermission)
                hasPermission
            },
            getCurrentLocation = {
                getCurrentLocation(context) { location, error ->
                    locationState = if (location != null) {
                        locationState.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        locationState.copy(
                            isLoading = false,
                            error = error ?: "Failed to get location"
                        )
                    }
                }
            }
        )
    }
}

class LocationPermissionState(
    val state: LocationState,
    val onStateChange: (LocationState) -> Unit,
    val requestPermission: () -> Unit,
    val checkPermission: () -> Boolean,
    val getCurrentLocation: () -> Unit
)

private fun getCurrentLocation(
    context: Context,
    onResult: (Location?, String?) -> Unit
) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    try {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onResult(location, null)
            } else {
                onResult(null, "Location is null")
            }
        }.addOnFailureListener { exception ->
            onResult(null, exception.message)
        }
    } catch (e: SecurityException) {
        onResult(null, "Permission denied")
    }
}
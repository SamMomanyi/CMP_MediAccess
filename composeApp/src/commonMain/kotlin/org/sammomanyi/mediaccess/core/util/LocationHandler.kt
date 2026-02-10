package org.sammomanyi.mediaccess.core.util

import androidx.compose.runtime.Composable

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false
)

// The interface commonMain uses to talk to the platform
expect class LocationHelper {
    fun requestPermission()
    fun checkPermission(): Boolean
    fun getCurrentLocation(onResult: (latitude: Double?, longitude: Double?, error: String?) -> Unit)
}

@Composable
expect fun rememberLocationHelper(onPermissionResult: (Boolean) -> Unit): LocationHelper
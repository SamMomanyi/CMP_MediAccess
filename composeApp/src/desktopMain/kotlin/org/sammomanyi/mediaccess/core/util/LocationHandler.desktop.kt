package org.sammomanyi.mediaccess.core.util

import androidx.compose.runtime.Composable

actual class LocationHelper {
    actual fun requestPermission() {
    }

    actual fun checkPermission(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun getCurrentLocation(onResult: (latitude: Double?, longitude: Double?, error: String?) -> Unit) {
    }
}

@Composable
actual fun rememberLocationHelper(onPermissionResult: (Boolean) -> Unit): LocationHelper {
    TODO("Not yet implemented")
}
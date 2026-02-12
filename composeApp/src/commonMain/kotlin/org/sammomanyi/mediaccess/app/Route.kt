package org.sammomanyi.mediaccess.app

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable object Welcome : Route
    @Serializable object AuthGraph : Route
    @Serializable object Login : Route
    @Serializable object Register : Route
    @Serializable object RegistrationOptions : Route

    @Serializable object MainGraph : Route
    @Serializable object Dashboard : Route
    @Serializable object Home : Route

    @Serializable data object Care : Route      // Renamed from Hospitals
    @Serializable data object Cover : Route     // New
    @Serializable data object Personal : Route  // Renamed from Profile
    @Serializable data object More : Route      // New

    // Additional Screens
    @Serializable object Notifications : Route
    @Serializable object Profile : Route
    @Serializable object Wellness : Route
}
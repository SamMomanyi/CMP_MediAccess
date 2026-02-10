package org.sammomanyi.mediaccess.app

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable object Welcome : Route
    @Serializable object AuthGraph : Route
    @Serializable object Login : Route
    @Serializable object Register : Route
    @Serializable object RegistrationOptions : Route

    @Serializable object MainGraph : Route
    @Serializable object Home : Route
    @Serializable object Records : Route
    @Serializable object Hospitals : Route
    @Serializable object Profile : Route
}
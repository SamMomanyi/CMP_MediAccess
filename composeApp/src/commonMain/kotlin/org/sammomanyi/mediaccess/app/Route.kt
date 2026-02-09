package org.sammomanyi.mediaccess.app

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable object AuthGraph : Route
    @Serializable object Login : Route
    @Serializable object Register : Route
    @Serializable object MainGraph : Route
    @Serializable object Dashboard : Route

}
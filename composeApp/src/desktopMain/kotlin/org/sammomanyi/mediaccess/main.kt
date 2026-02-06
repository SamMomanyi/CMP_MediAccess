package org.sammomanyi.mediaccess

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.sammomanyi.mediaccess.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MediAccess",
    ) {
        App()
    }
}
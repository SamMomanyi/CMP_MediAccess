package org.sammomanyi.mediaccess

import org.koin.core.context.startKoin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.sammomanyi.mediaccess.app.App
import org.sammomanyi.mediaccess.di.platformModule
import org.sammomanyi.mediaccess.di.sharedModule
import org.sammomanyi.mediaccess.features.identity.presentation.verification.VerificationScreen

//moved outside application

fun main() {


    // Start Koin BEFORE the application block
    startKoin {
        modules(sharedModule, platformModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MediAccess",
        ) {
            VerificationScreen()
        }
    }
}
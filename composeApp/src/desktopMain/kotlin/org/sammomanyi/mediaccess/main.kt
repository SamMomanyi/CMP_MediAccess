package org.sammomanyi.mediaccess

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import org.sammomanyi.mediaccess.di.platformModule
import org.sammomanyi.mediaccess.di.sharedModule
import org.sammomanyi.mediaccess.features.admin.domain.model.Admin
import org.sammomanyi.mediaccess.features.admin.presentation.AdminAuthScreen

//moved outside application

fun main() {



    // Start Koin BEFORE the application block
    startKoin {
        modules(sharedModule, platformModule)
    }

    application {
        var loggedInAdmin by remember { mutableStateOf<Admin?>(null) }

        Window(
            onCloseRequest = ::exitApplication,
            title = if (loggedInAdmin != null) "MediAccess Admin — ${loggedInAdmin!!.name}"
            else "MediAccess Admin",
            state = WindowState(width = 960.dp, height = 700.dp)
        ) {
            if (loggedInAdmin == null) {
                AdminAuthScreen(
                    onAuthenticated = { admin -> loggedInAdmin = admin }
                )
            } else {
                AdminDashboard(
                    admin = loggedInAdmin!!,
                    onLogout = { loggedInAdmin = null }
                )
            }
        }
    }
}
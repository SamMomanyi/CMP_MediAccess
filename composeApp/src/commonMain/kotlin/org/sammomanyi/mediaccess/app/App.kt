package org.sammomanyi.mediaccess.app

import GoogleSignInProvider
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessTheme
import org.sammomanyi.mediaccess.core.presentation.theme.ThemeViewModel
import org.sammomanyi.mediaccess.features.identity.domain.auth.GoogleSignInResult
import org.sammomanyi.mediaccess.features.identity.presentation.checkin.CheckInViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.link_cover.LinkCoverScreen
import org.sammomanyi.mediaccess.features.identity.presentation.login.LoginScreen
import org.sammomanyi.mediaccess.features.identity.presentation.registration.RegistrationOptionsDialog
import org.sammomanyi.mediaccess.features.identity.presentation.registration.RegistrationScreen
import org.sammomanyi.mediaccess.features.identity.presentation.waitingroom.WaitingRoomScreen
import org.sammomanyi.mediaccess.features.identity.presentation.welcome.WelcomeScreen
import org.sammomanyi.mediaccess.features.pharmacy.presentation.ExpenditureSummaryScreen

@Composable
fun App() {

    // Observe theme from ViewModel
    val themeViewModel: ThemeViewModel = koinViewModel()
    val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

    MediAccessTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        //val context = androidx.compose.ui.platform.LocalContext.current
        val googleSignInProvider: GoogleSignInProvider = koinInject()
        var showRegistrationOptions by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()



        fun handleGoogleRegister() {
            scope.launch {
                when (val result = googleSignInProvider.signIn()) {
                    is GoogleSignInResult.Success -> {
                        navController.navigate(Route.MainGraph) {
                            popUpTo(Route.Welcome) { inclusive = true }
                        }
                    }
                    is GoogleSignInResult.Error -> {
                        println("Google Registration Error: ${result.message}")
                    }
                    GoogleSignInResult.Cancelled -> {
                        println("Google Registration Cancelled")
                    }
                }
            }
        }

        if (showRegistrationOptions) {
            RegistrationOptionsDialog(
                onDismiss = { showRegistrationOptions = false },
                onEmailRegister = {
                    showRegistrationOptions = false
                    navController.navigate(Route.Register)
                },
                onGoogleRegister = {
                    showRegistrationOptions = false
                    handleGoogleRegister()
                }
            )
        }


        NavHost(
            navController = navController,
            startDestination = Route.Welcome
        ) {
            // Welcome Screen (Entry Point)
            composable<Route.Welcome> {
                WelcomeScreen(
                    onLoginClick = {
                        navController.navigate(Route.Login)
                    },
                    onRegisterClick = {
                        showRegistrationOptions = true
                    }
                )
            }

            // Authentication Flow
            navigation<Route.AuthGraph>(startDestination = Route.Login) {
                composable<Route.Login> {
                    LoginScreen(
                        onRegisterClick = {
                            showRegistrationOptions = true
                        },
                        onLoginSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo(Route.Welcome) { inclusive = true }
                            }
                        },
                        onBackClick = {
                            navController.navigate(Route.Welcome) {
                                popUpTo(Route.Welcome) { inclusive = true }
                            }
                        }
                    )
                }

                composable<Route.Register> {
                    RegistrationScreen(
                        onLoginClick = {
                            navController.popBackStack()
                            navController.navigate(Route.Login)
                        },
                        onSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo(Route.Welcome) { inclusive = true }
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

// Main App with Bottom Navigation
            // FIX: Ensure the composable route matches the startDestination, NOT the graph route
// In App.kt
            navigation<Route.MainGraph>(startDestination = Route.Dashboard) {
                composable<Route.Dashboard> {
                    MainScreen(
                        onLogout = {
                            navController.navigate(Route.Welcome) {
                                popUpTo(Route.MainGraph) { inclusive = true }
                            }
                        },
                        // ✅ Pass LinkCover navigation into MainScreen
                        onNavigateToLinkCover = {
                            navController.navigate(Route.LinkCover)
                        }
                    )
                }
            }

            // ✅ LinkCover is a full screen OUTSIDE the bottom nav graph
            composable<Route.LinkCover> {
                LinkCoverScreen(
                    onBackClick = { navController.popBackStack() },
                    onLinkSuccess = { navController.popBackStack() }
                )
            }

            composable<Route.PharmacySummary> {
                ExpenditureSummaryScreen(
                    onDone = {
                        navController.navigate(Route.Dashboard) {
                            popUpTo(Route.Dashboard) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.WaitingRoom> {
                val checkInViewModel: CheckInViewModel = koinViewModel()
                val state by checkInViewModel.state.collectAsStateWithLifecycle()

                WaitingRoomScreen(
                    queueState = state.queueState,
                    onBack = { navController.navigateUp() },
                    onNavigateToPharmacy = {  // ← ADD THIS
                        navController.navigate(Route.PharmacySummary) {
                            popUpTo(Route.WaitingRoom) { inclusive = true }
                        }
                    }
                )
            }

        }
    }
}

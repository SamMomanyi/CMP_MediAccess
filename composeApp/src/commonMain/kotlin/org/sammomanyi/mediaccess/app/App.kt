package org.sammomanyi.mediaccess.app



import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.dashboard.DashboardScreen
import org.sammomanyi.mediaccess.features.identity.presentation.dashboard.DashboardViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.login.LoginScreen
import org.sammomanyi.mediaccess.features.identity.presentation.registration.RegistrationScreen



@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Route.AuthGraph
        ) {
            // Authentication Flow
            navigation<Route.AuthGraph>(startDestination = Route.Login) {
                composable<Route.Login> {
                    LoginScreen(
                        onRegisterClick = {
                            navController.navigate(Route.Register)
                        },
                        onLoginSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo(Route.AuthGraph) { inclusive = true }
                            }
                        }
                    )
                }

                composable<Route.Register> {
                    RegistrationScreen(
                        onLoginClick = {
                            navController.popBackStack() // Go back to Login
                        },
                        onSuccess = {
                            navController.navigate(Route.MainGraph) {
                                popUpTo(Route.AuthGraph) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Main App Flow (Dashboard/VisitCode)
            navigation<Route.MainGraph>(startDestination = Route.Dashboard) {
                composable<Route.Dashboard> {
                    val viewModel: DashboardViewModel = koinViewModel()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    DashboardScreen(
                        state = state, // Pass the collected state
                        onGenerateCodeClick = { viewModel.onGenerateVisitCode() }, // Trigger the VM function
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

// Simple placeholders to prevent "Unresolved reference" errors
                composable<Route.Records> { PlaceholderScreen("Medical Records") }
                composable<Route.Hospitals> { PlaceholderScreen("Nearby Hospitals") }
                composable<Route.Profile> { PlaceholderScreen("User Profile") }

            }

            @Composable
            fun PlaceholderScreen(title: String) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(title, style = MaterialTheme.typography.headlineMedium)
                }
            }
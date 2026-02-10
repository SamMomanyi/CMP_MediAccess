package org.sammomanyi.mediaccess.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.sammomanyi.mediaccess.features.identity.presentation.home.HomeScreen
import org.sammomanyi.mediaccess.features.identity.presentation.hospitals.HospitalsScreen
import org.sammomanyi.mediaccess.features.identity.presentation.profile.ProfileScreen
import org.sammomanyi.mediaccess.features.identity.presentation.records.RecordsScreen

data class BottomNavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val bottomNavItems = listOf(
        BottomNavItem(Route.Home, "Home", Icons.Default.Home),
        BottomNavItem(Route.Records, "Records", Icons.AutoMirrored.Filled.List),
        BottomNavItem(Route.Hospitals, "Hospitals", Icons.Default.Place),
        BottomNavItem(Route.Profile, "Profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = navBackStackEntry?.destination?.route == item.route::class.qualifiedName

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home
        ) {
            composable<Route.Home> {
                HomeScreen(padding = padding)
            }

            composable<Route.Records> {
                RecordsScreen(padding = padding)
            }

            composable<Route.Hospitals> {
                HospitalsScreen(padding = padding)
            }

            composable<Route.Profile> {
                ProfileScreen(
                    padding = padding,
                    onLogout = onLogout
                )
            }
        }
    }
}
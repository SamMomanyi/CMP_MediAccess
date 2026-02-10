package org.sammomanyi.mediaccess.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.sammomanyi.mediaccess.features.identity.presentation.care.CareScreen
import org.sammomanyi.mediaccess.features.identity.presentation.cover.CoverScreen
import org.sammomanyi.mediaccess.features.identity.presentation.home.HomeScreen
import org.sammomanyi.mediaccess.features.identity.presentation.more.MoreScreen
import org.sammomanyi.mediaccess.features.identity.presentation.personal.PersonalScreen

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
        BottomNavItem(Route.Care, "Care", Icons.Default.Favorite),
        BottomNavItem(Route.Cover, "Cover", Icons.Default.Shield),
        BottomNavItem(Route.Personal, "Personal", Icons.Default.Person),
        BottomNavItem(Route.More, "More", Icons.Default.Menu)
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
                HomeScreen(
                    padding = padding,
                    onNavigateToHospitals = {
                        navController.navigate(Route.Care)
                    }
                )
            }

            composable<Route.Care> {
                CareScreen(padding = padding)
            }

            composable<Route.Cover> {
                CoverScreen(padding = padding)
            }

            composable<Route.Personal> {
                PersonalScreen(
                    padding = padding,
                    onLogout = onLogout
                )
            }

            composable<Route.More> {
                MoreScreen(padding = padding)
            }
        }
    }
}
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
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.features.identity.presentation.care.CareScreen
import org.sammomanyi.mediaccess.features.identity.presentation.cover.CoverScreen
import org.sammomanyi.mediaccess.features.identity.presentation.home.HomeScreen
import org.sammomanyi.mediaccess.features.identity.presentation.more.MoreScreen
import org.sammomanyi.mediaccess.features.identity.presentation.notifications.NotificationsScreen
import org.sammomanyi.mediaccess.features.identity.presentation.personal.PersonalScreen
import org.sammomanyi.mediaccess.features.wellness.presentation.WellnessScreen

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

    // Check if current route should show bottom nav
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavItems.map { it.route::class.qualifiedName }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    contentColor = MediAccessColors.Primary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected =
                            navBackStackEntry?.destination?.route == item.route::class.qualifiedName

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
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) MediAccessColors.Primary else MediAccessColors.TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (selected) MediAccessColors.Primary else MediAccessColors.TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MediAccessColors.Primary,
                                selectedTextColor = MediAccessColors.Primary,
                                indicatorColor = MediAccessColors.PrimaryLight.copy(alpha = 0.2f)
                            )
                        )
                    }
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
                    },
                    onNavigateToBenefits = {
                        // Benefits dialog is handled in HomeScreen
                    },
                    onNavigateToSpent = {
                        // Spent dialog is handled in HomeScreen
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Route.Notifications)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Route.Personal)
                    },
                    onNavigateToWellness = { navController.navigate(Route.Wellness) }
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
                MoreScreen(
                    padding = padding,
                    onLogout = onLogout
                )
            }

            composable<Route.Notifications> {
                NotificationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Route.Wellness> {
                WellnessScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
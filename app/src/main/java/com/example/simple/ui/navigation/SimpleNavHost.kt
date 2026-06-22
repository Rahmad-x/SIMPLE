package com.example.simple.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simple.domain.model.UserRole
import com.example.simple.ui.components.SimpleBottomNavBar
import com.example.simple.ui.screens.admin.AdminDashboardScreen
import com.example.simple.ui.screens.borrow.BorrowScreen
import com.example.simple.ui.screens.catalog.CatalogScreen
import com.example.simple.ui.screens.history.HistoryScreen
import com.example.simple.ui.screens.home.HomeScreen
import com.example.simple.ui.screens.login.LoginScreen
import com.example.simple.ui.screens.onboarding.OnboardingScreen
import com.example.simple.ui.screens.profile.ProfileScreen

/** Routes yang menampilkan bottom navigation bar. */
private val mainRoutes = setOf(
    Route.Home.route,
    Route.Catalog.route,
    Route.Borrow.route,
    Route.History.route,
    Route.AdminDashboard.route,
    Route.Profile.route,
)

@Composable
fun SimpleNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Login.route,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // TODO: ganti dengan role user aktif yang sesungguhnya (dari HomeViewModel/SessionManager).
    var isAdmin by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (currentRoute in mainRoutes) {
                SimpleBottomNavBar(
                    currentRoute = currentRoute,
                    isAdmin = isAdmin,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            popUpTo(Route.Home.route) { saveState = true }
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Route.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Route.Onboarding.route) {
                                popUpTo(Route.Login.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Route.Onboarding.route) {
                    OnboardingScreen(
                        onSetupComplete = {
                            navController.navigate(Route.Home.route) {
                                popUpTo(Route.Onboarding.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Route.Home.route) {
                    HomeScreen(
                        onNavigateToCatalog = { navController.navigate(Route.Catalog.route) },
                        onNavigateToBorrow = { navController.navigate(Route.Borrow.route) },
                        onNavigateToHistory = { navController.navigate(Route.History.route) },
                    )
                }

                composable(Route.Catalog.route) {
                    CatalogScreen(
                        onItemClick = { itemId ->
                            navController.navigate(Route.ItemDetail.createRoute(itemId))
                        },
                    )
                }

                composable(Route.Borrow.route) {
                    BorrowScreen(
                        onRequestSubmitted = {
                            navController.navigate(Route.History.route) {
                                popUpTo(Route.Borrow.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Route.History.route) {
                    HistoryScreen()
                }

                composable(Route.AdminDashboard.route) {
                    AdminDashboardScreen()
                }

                composable(Route.Profile.route) {
                    ProfileScreen(
                        onLoggedOut = {
                            isAdmin = false
                            navController.navigate(Route.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }
            }
        }
    }
}
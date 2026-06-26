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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simple.domain.model.UserRole
import com.example.simple.ui.components.SimpleBottomNavBar
import com.example.simple.worker.ReminderWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.compose.ui.platform.LocalContext
import com.example.simple.ui.screens.admin.AddItemScreen
import com.example.simple.ui.screens.admin.AdminDashboardScreen
import com.example.simple.ui.screens.borrow.BorrowFormScreen
import com.example.simple.ui.screens.borrow.BorrowScreen
import com.example.simple.ui.screens.catalog.CatalogScreen
import com.example.simple.ui.screens.catalog.ItemDetailScreen
import com.example.simple.ui.screens.history.HistoryScreen
import com.example.simple.ui.screens.home.HomeScreen
import com.example.simple.ui.screens.home.HomeViewModel
import com.example.simple.ui.screens.login.LoginScreen
import com.example.simple.ui.screens.onboarding.OnboardingScreen
import com.example.simple.ui.screens.profile.EditProfileScreen
import com.example.simple.ui.screens.profile.ProfileScreen

/** Routes yang menampilkan bottom navigation bar. */
private val mainRoutes = setOf(
    Route.Home.route,
    Route.Catalog.route,
    Route.History.route,
    Route.AdminDashboard.route,
    Route.Profile.route,
)

@Composable
fun SimpleNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Login.route,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val activeOrg by homeViewModel.activeOrganization.collectAsState()
    val isAdmin = activeOrg?.role == UserRole.ADMIN || activeOrg?.role == UserRole.STAFF
    val context = LocalContext.current

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
                        onLoginSuccess = { user ->
                            // Trigger notification check immediately on login
                            val immediateRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
                            WorkManager.getInstance(context).enqueue(immediateRequest)

                            val destination = if (user.activeOrgId != null) {
                                Route.Home.route
                            } else {
                                Route.Onboarding.route
                            }
                            navController.navigate(destination) {
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
                        onNavigateToBorrow = { navController.navigate(Route.Catalog.route) },
                        onNavigateToHistory = { navController.navigate(Route.History.route) },
                    )
                }

                composable(Route.Catalog.route) {
                    CatalogScreen(
                        onItemClick = { orgId, itemId ->
                            navController.navigate(Route.ItemDetail.createRoute(orgId, itemId))
                        },
                    )
                }

                composable(Route.ItemDetail.route) {
                    ItemDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBorrowForm = { orgId, itemId ->
                            navController.navigate(Route.BorrowForm.createRoute(orgId, itemId))
                        }
                    )
                }

                composable(Route.BorrowForm.route) {
                    BorrowFormScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate(Route.History.route) {
                                popUpTo(Route.Catalog.route) { saveState = true }
                            }
                        }
                    )
                }

                composable(Route.History.route) {
                    HistoryScreen()
                }

                composable(Route.AdminDashboard.route) {
                    AdminDashboardScreen(
                        onNavigateToAddItem = {
                            navController.navigate(Route.AdminAddItem.route)
                        }
                    )
                }

                composable(Route.AdminAddItem.route) {
                    AddItemScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Route.Profile.route) {
                    ProfileScreen(
                        onLoggedOut = {
                            navController.navigate(Route.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Route.EditProfile.route)
                        }
                    )
                }

                composable(Route.EditProfile.route) {
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
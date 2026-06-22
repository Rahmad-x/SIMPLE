package com.example.simple.ui.navigation

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object SignUp : Route("signup")
    data object Onboarding : Route("onboarding")

    data object Home : Route("home")
    data object Catalog : Route("catalog")
    data object ItemDetail : Route("item/{itemId}") {
        fun createRoute(itemId: String) = "item/$itemId"
    }

    data object Borrow : Route("borrow")
    data object BorrowConfirm : Route("borrow-confirm/{itemId}") {
        fun createRoute(itemId: String) = "borrow-confirm/$itemId"
    }

    data object History : Route("history")

    data object AdminDashboard : Route("admin")

    data object Profile : Route("profile")
}
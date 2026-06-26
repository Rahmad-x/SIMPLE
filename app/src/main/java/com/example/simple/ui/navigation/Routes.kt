package com.example.simple.ui.navigation

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object SignUp : Route("signup")
    data object Onboarding : Route("onboarding")

    data object Home : Route("home")
    data object Catalog : Route("catalog")
    data object ItemDetail : Route("item/{orgId}/{itemId}") {
        fun createRoute(orgId: String, itemId: String) = "item/$orgId/$itemId"
    }
    data object BorrowForm : Route("borrow-form/{orgId}/{itemId}") {
        fun createRoute(orgId: String, itemId: String) = "borrow-form/$orgId/$itemId"
    }

    data object History : Route("history")

    data object AdminDashboard : Route("admin")
    data object AdminAddItem : Route("admin/add-item")

    data object Profile : Route("profile")
    data object EditProfile : Route("profile/edit")
}
package com.example.simple.ui.screens.home

import com.example.simple.domain.model.Transaction

data class HomeStats(
    val totalItems: Int,
    val availableItems: Int,
    val onLoan: Int,
    val overdue: Int,
)

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(val stats: HomeStats, val recentRequests: List<Transaction>) : HomeState()
    data class Error(val message: String) : HomeState()
}
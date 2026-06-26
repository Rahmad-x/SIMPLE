package com.example.simple.domain.util

import java.util.concurrent.TimeUnit
import kotlin.math.max

object FeeCalculator {

    /**
     * Calculates the estimated rental fee based on item price and duration.
     */
    fun calculateRentalFee(
        rentalPrice: Double,
        startDate: Long,
        endDate: Long
    ): Double {
        if (rentalPrice <= 0) return 0.0
        val diffInMillies = max(0, endDate - startDate)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies)
        // Minimum 1 day charge if there's any duration
        val chargeDays = if (diffInDays == 0L && diffInMillies > 0) 1L else diffInDays
        return rentalPrice * chargeDays
    }

    /**
     * Calculates the late fee based on organization policy and return date.
     */
    fun calculateLateFee(
        lateFeePerDay: Double,
        dueDate: Long,
        returnDate: Long
    ): Double {
        if (lateFeePerDay <= 0 || returnDate <= dueDate) return 0.0
        val diffInMillies = returnDate - dueDate
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies)
        val lateDays = if (diffInDays == 0L && diffInMillies > 0) 1L else diffInDays
        return lateFeePerDay * lateDays
    }
}
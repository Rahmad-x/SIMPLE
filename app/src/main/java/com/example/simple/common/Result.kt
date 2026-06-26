package com.example.simple.common
sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()
}


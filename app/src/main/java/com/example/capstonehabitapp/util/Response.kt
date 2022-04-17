package com.example.capstonehabitapp.util

sealed class Response<out T> {
    data class Success<out T>(val data: T): Response<T>()
    data class Failure<out T>(val message: String): Response<T>()
    class Loading<out T>: Response<T>()
}
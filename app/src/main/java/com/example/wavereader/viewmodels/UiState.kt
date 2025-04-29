package com.example.wavereader.viewmodels

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String? = null) : UiState<Nothing>
}
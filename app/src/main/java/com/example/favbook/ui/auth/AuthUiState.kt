package com.example.favbook.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
)

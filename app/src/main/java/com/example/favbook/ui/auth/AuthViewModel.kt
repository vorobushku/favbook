package com.example.favbook.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Пожалуйста, заполните все поля") }
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.update { it.copy(errorMessage = null) }
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.update { it.copy(errorMessage = exception.message) }
            }
    }

    fun signUp(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Пожалуйста, заполните все поля") }
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.update { it.copy(errorMessage = null) }
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.update { it.copy(errorMessage = exception.message) }
            }
    }
}
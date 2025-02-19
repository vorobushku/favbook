package com.example.favbook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun rememberFirebaseUser(): FirebaseUser? {
    val userState = produceState<FirebaseUser?>(initialValue = null) {
        value = FirebaseAuth.getInstance().currentUser
    }
    return userState.value
}

@Composable
fun rememberAuth(): FirebaseAuth {
    return remember { FirebaseAuth.getInstance() }
}
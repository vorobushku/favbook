package com.example.favbook.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.favbook.rememberAuth

@Composable
fun AuthScreen(navController: NavHostController) {
    val auth = rememberAuth()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        //horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Auth",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(start = 15.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    if (email.value.isBlank() || password.value.isBlank()) {
                        errorMessage.value = "Пожалуйста, заполните все поля"
                    } else {
                        errorMessage.value = null
                        auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim())
                            .addOnSuccessListener {
                                navController.navigate("main_screen") {
                                    popUpTo("auth_screen") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { errorMessage.value = it.message }
                    }
                },
                modifier = Modifier.weight(1f).size(55.dp)
            ) {
                Text("Log In")
            }

            Button(
                onClick = {
                    if (email.value.isBlank() || password.value.isBlank()) {
                        errorMessage.value = "Пожалуйста, заполните все поля"
                    } else {
                        errorMessage.value = null
                        auth.createUserWithEmailAndPassword(
                            email.value.trim(),
                            password.value.trim()
                        )
                            .addOnSuccessListener {
                                navController.navigate("main_screen") {
                                    popUpTo("auth_screen") { inclusive = true }
                                }
                            }
                            .addOnFailureListener { errorMessage.value = it.message }
                    }
                },
                modifier = Modifier.weight(1f).size(55.dp)
            ) {
                Text("Sign Up")
            }
        }

        errorMessage.value?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(15.dp))
        }
    }
}
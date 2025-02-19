package com.example.favbook

import com.example.favbook.ui.BottomBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val user = rememberFirebaseUser()
            val startDestination = if (user == null) "auth_screen" else "main_screen"

            // Создаем Scaffold с навигацией
            Scaffold(
                bottomBar = { BottomBar(navController) } // Нижний бар с навигацией
            ) { innerPadding ->
                // Основной контент
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigator(navController,startDestination) // Обновленная навигация
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        val navController = rememberNavController()
        // В Preview передаем корректный Scaffold с innerPadding
        Scaffold(
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                //AppNavigator(navController)
            }
        }
    }
}
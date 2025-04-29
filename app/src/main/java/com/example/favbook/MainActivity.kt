package com.example.favbook

import com.example.favbook.ui.BottomBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val user = rememberFirebaseUser()
            val startDestination = if (user == null) Screen.Auth.route else Screen.Main.route

            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

            Scaffold(
                bottomBar = {
                    if (currentDestination in Screen.bottomBarScreens) {
                        BottomBar(navController)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigator(navController,startDestination)
                }
            }
        }
    }
}
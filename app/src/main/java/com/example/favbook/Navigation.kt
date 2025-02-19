package com.example.favbook

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.favbook.screens.AuthScreen
import com.example.favbook.screens.MainScreen
import com.example.favbook.screens.BookScreen
import com.example.favbook.screens.SearchScreen
import com.example.favbook.screens.AddScreen
import com.example.favbook.screens.BookDetailScreen

@Composable
fun AppNavigator(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth_screen") { AuthScreen(navController) }
        composable("main_screen") { MainScreen(navController) }
        composable("book_screen") { BookScreen() }
        composable("search_screen") { SearchScreen(navController) }
        composable("add_screen") { AddScreen() }
        composable("book_detail_screen/{title}/{coverUrl}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Без названия"
            val coverUrl = backStackEntry.arguments?.getString("coverUrl") ?: ""
            BookDetailScreen(title, coverUrl)
        }
    }
}
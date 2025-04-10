package com.example.favbook

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.favbook.screens.AuthScreen
import com.example.favbook.screens.MainScreen
import com.example.favbook.screens.BookScreen
import com.example.favbook.screens.SearchScreen
import com.example.favbook.screens.AddScreen
import com.example.favbook.screens.AuthorBooksScreen
import com.example.favbook.screens.BookDetailScreen
import com.example.favbook.screens.CategoryBooksScreen
import java.net.URLDecoder

@Composable
fun AppNavigator(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) { AuthScreen(navController) }
        composable(Screen.Main.route) { MainScreen(navController) }
        composable(Screen.Book.route) { BookScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.Add.route) { AddScreen(navController) }
        composable(Screen.BookDetail.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("coverUrl") { type = NavType.StringType },
                navArgument("authors") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val coverUrl = backStackEntry.arguments?.getString("coverUrl") ?: ""
            val authors = backStackEntry.arguments?.getString("authors") ?: ""

            BookDetailScreen(title = title, coverUrl = coverUrl, authors = authors, navController = navController)
        }
        composable(Screen.CategoryBooks.route) { backStackEntry ->
            // Получаем category, которая была передана
            val category = backStackEntry.arguments?.getString("category")?.let {
                URLDecoder.decode(it, "UTF-8") // Раскодируем обратно
            } ?: ""
            Log.d("NavigationDebug", "Navigated to category: $category")
            CategoryBooksScreen(category, navController)
        }
        composable(Screen.AuthorBooks.route) { backStackEntry ->
            val author = backStackEntry.arguments?.getString("author") ?: ""
            AuthorBooksScreen(author = author, navController = navController)
        }

    }
}
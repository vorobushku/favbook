package com.example.favbook.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.favbook.data.model.AnyBook
import com.example.favbook.ui.auth.AuthScreen
import com.example.favbook.ui.main.MainScreen
import com.example.favbook.ui.book.BookScreen
import com.example.favbook.ui.search.SearchScreen
import com.example.favbook.ui.add.AddScreen
import com.example.favbook.ui.author_books.AuthorBooksScreen
import com.example.favbook.ui.book_details.BookDetailScreen
import com.example.favbook.ui.category_books.CategoryBooksScreen
import java.net.URLDecoder

@Composable
fun AppNavigator(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) { AuthScreen(navController) }
        composable(Screen.Main.route) { MainScreen(navController) }
        composable(Screen.Book.route) { BookScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.Add.route) { AddScreen(navController) }
        composable(Screen.BookDetail.route) {
            val anyBook = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<AnyBook>("book")

            anyBook?.let {
                BookDetailScreen(anyBook = it, navController = navController)
            }
        }
        composable(Screen.CategoryBooks.route) { backStackEntry ->
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
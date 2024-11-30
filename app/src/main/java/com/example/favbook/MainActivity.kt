package com.example.favbook

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.favbook.ui.theme.FavbookTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Properties
import com.example.favbook.BuildConfig // Пример правильного импорта


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // Создаем Scaffold с навигацией
            Scaffold(
                bottomBar = { BottomBar(navController) } // Нижний бар с навигацией
            ) { innerPadding ->
                // Основной контент
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigator(navController) // Обновленная навигация
                }
            }
        }

        try {
            val apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
            Log.d("API_KEY", "Google Books API Key: $apiKey")
        } catch (e: Exception) {
            Log.e("API_KEY", "Error retrieving API key: ${e.message}")
            }
    }
}

@Composable
fun AppNavigator(navController: NavHostController) {

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController) }
        composable("book_screen") { BookScreen() }
        composable("search_screen") { SearchScreen() }
        composable("add_screen") { AddScreen() }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Главный экран",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun BookScreen() {
    // Просто пустой экран для демонстрации
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Экран книг", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Экран поиска:", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun AddScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Экран добавления", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    BottomAppBar(
        //цвет тулбара и высота
        containerColor = Color.LightGray,
        modifier = Modifier.height(65.dp)
    ) {
        //кнопка домой
        IconButton(onClick = {
            navController.navigate("main_screen") {
                // Очистка стека, чтобы не было дублирования экранов
                popUpTo("main_screen") { inclusive = true }
            }
        },
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = "Главная страница")
        }
        // Spacer для выравнивания
        Spacer(modifier = Modifier.weight(1f)) // Центрируем другие кнопки

        // Кнопка "Книжка"
        IconButton(onClick = { navController.navigate("book_screen") }) {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = "Списки"
            )
        }

        // Spacer для выравнивания
        Spacer(modifier = Modifier.weight(1f)) // Заполняет пространство между иконками

        // Кнопка "Поиск"
        IconButton(onClick = { navController.navigate("search_screen") }) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Поиск"
            )
        }

        // Spacer для выравнивания
        Spacer(modifier = Modifier.weight(1f)) // Заполняет пространство между иконками

        // Кнопка "Добавить" (Плюсик)
        IconButton(
            onClick = { navController.navigate("add_screen") },
            modifier = Modifier.padding(end = 16.dp) // Добавляем отступ от правого края
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Добавить"
            )
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
            AppNavigator(navController)
        }
    }
}
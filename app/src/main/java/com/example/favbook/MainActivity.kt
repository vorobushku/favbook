package com.example.favbook

import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FavbookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController) }
        // Здесь можно добавить другие экраны
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Главный экран",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxSize()
            )
        }
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
        IconButton(onClick = { navController.navigate("main_screen") }) {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = "Списки"
            )
        }

        // Spacer для выравнивания
        Spacer(modifier = Modifier.weight(1f)) // Заполняет пространство между иконками

        // Кнопка "Поиск"
        IconButton(onClick = { navController.navigate("main_screen") }) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Поиск"
            )
        }

        // Spacer для выравнивания
        Spacer(modifier = Modifier.weight(1f)) // Заполняет пространство между иконками

        // Кнопка "Добавить" (Плюсик)
        IconButton(
            onClick = { navController.navigate("main_screen") },
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
    AppNavigator()
}
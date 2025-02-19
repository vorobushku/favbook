package com.example.favbook.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun BottomBar(navController: NavHostController) {
    BottomAppBar(
        //цвет тулбара и высота
        containerColor = Color.LightGray,
        modifier = Modifier.height(65.dp)
    ) {
        //кнопка домой
        IconButton(
            onClick = {
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
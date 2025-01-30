package com.example.favbook

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.favbook.ui.theme.FavbookTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import java.util.Properties
import com.example.favbook.BuildConfig // Пример правильного импорта
import com.example.favbook.data.model.BookDoc
import com.example.favbook.data.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

        // Тестирование подключения к API
        lifecycleScope.launch {
            testApiConnection()
        }

    }
    
    // Тестирование подключения к API
    private suspend fun testApiConnection() {
//        // Вызов API
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.api.searchBooks("Гарри Поттер", BuildConfig.GOOGLE_BOOKS_API_KEY)
//                Log.d("API_RESPONSE", "Full response: $response")
//                if (response.items.isNullOrEmpty()) {
//                    Log.e("API", "No items found in the response")
//                } else {
//                    Log.d("API_RESPONSE", "Fetched books: ${response.items.map { it.volumeInfo.title }}")
//                }
//            } catch (e: Exception) {
//                Log.e("API_ERROR", "Error fetching books", e)
//            }
//        }

        try {
            val response = RetrofitInstance.api.searchBooks("Гордость")
            val books = response.docs ?: emptyList() // Безопасная проверка нarry Potterа null

            if (books.isNotEmpty()) {
                books.forEach { book ->
                    Log.d("API_RESPONSE", "Book: ${book.title}, Authors: ${book.author_name?.joinToString(", ") ?: "Unknown"}")
                }
            } else {
                Log.e("API_RESPONSE", "No books found.")
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error fetching books: ${e.message}")
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController) }
        composable("book_screen") { BookScreen() }
        composable("search_screen") { SearchScreen(navController) }
        composable("add_screen") { AddScreen() }
        // Экран деталей книги
        composable("book_detail_screen/{title}/{coverUrl}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Без названия"
            val coverUrl = backStackEntry.arguments?.getString("coverUrl") ?: ""
            BookDetailScreen(title, coverUrl)
        }
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
fun SearchScreen(navController: NavHostController) {
    val searchQuery = remember { mutableStateOf("") }
    val books = remember { mutableStateOf<List<BookDoc>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Строка поиска
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Введите название книги") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка поиска
        Button(
            onClick = {
                searchBooksApi(searchQuery.value, books, errorMessage)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Поиск")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Результаты или ошибка
        if (!errorMessage.value.isNullOrEmpty()) {
            Text(
                text = errorMessage.value ?: "",
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                items(books.value) { book ->
                    BookItem(book, navController) // Передаем navController
                }
            }
        }
    }
}

@Composable
fun BookItem(book: BookDoc, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray)
            .padding(8.dp)
            .clickable {
                val encodedTitle = Uri.encode(book.title) // Кодируем для передачи
                val encodedCoverUrl = Uri.encode(book.coverUrl ?: "")

                navController.navigate("book_detail_screen/$encodedTitle/$encodedCoverUrl")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Загружаем обложку, если есть
        book.coverUrl?.let { coverUrl ->
            AsyncImage(
                model = coverUrl,
                contentDescription = "Обложка книги",
                modifier = Modifier
                    .size(50.dp) // Размер обложки
                    .padding(end = 8.dp)
            )
        }

        Column {
            Text(text = "Название: ${book.title}", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Авторы: ${book.author_name?.joinToString(", ") ?: "Неизвестно"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Запрос к API
fun searchBooksApi(
    query: String,
    booksState: MutableState<List<BookDoc>>,
    errorState: MutableState<String?>
) {
    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            val response = RetrofitInstance.api.searchBooks(query)
            //val books = response.docs ?: emptyList()
            val books = response.docs?.filter { it.language?.contains("eng") == true } ?: emptyList()

            withContext(Dispatchers.Main) {
                if (books.isNotEmpty()) {
                    booksState.value = books
                    errorState.value = null
                } else {
                    errorState.value = "Книги не найдены"
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorState.value = "Ошибка: ${e.message}"
            }
        }
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
fun BookDetailScreen(title: String, coverUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Если есть обложка, показываем её
            if (coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Обложка книги",
                    modifier = Modifier.size(200.dp) // Увеличенный размер
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
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
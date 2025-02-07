package com.example.favbook

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.favbook.data.model.BookItem
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

    //Возможно стоит удалить navController
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
        val books = remember { mutableStateOf<List<BookItem>>(emptyList()) }
        val errorMessage = remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
    fun BookItem(book: BookItem, navController: NavHostController) {
        // Заменяем http на https в URL, если это возможно
        val coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.LightGray)
                .padding(8.dp)
                .clickable {
                    val encodedTitle = Uri.encode(book.volumeInfo.title)
                    val encodedCoverUrl = Uri.encode(coverUrl)
                    navController.navigate("book_detail_screen/$encodedTitle/$encodedCoverUrl")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = coverUrl, // Используем https
                    contentDescription = "Обложка книги",
                    modifier = Modifier.size(65.dp),
                    placeholder = painterResource(R.drawable.placeholder),
                    error = painterResource(R.drawable.error),
                    onError = { e ->
                        Log.e("AsyncImage", "Ошибка загрузки: ${e.result.throwable.message}")
                    }
                )
            } else {
                Log.e("BookCover", "Cover URL is empty!")
            }

            Column {
                Text(
                    text = "Название: ${book.volumeInfo.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Авторы: ${book.volumeInfo.authors?.joinToString(", ") ?: "Неизвестно"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Запрос к API
    private fun searchBooksApi(
        query: String,
        booksState: MutableState<List<BookItem>>,
        errorState: MutableState<String?>
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val response =
                    RetrofitInstance.api.searchBooks(query, BuildConfig.GOOGLE_BOOKS_API_KEY)
                val books = response.items ?: emptyList()

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
                Log.d("CoverUrl", coverUrl)
                // Если есть обложка, показываем её
                if (coverUrl.isNotEmpty()) {
                    AsyncImage(
                        model = coverUrl, // Используем https
                        contentDescription = "Обложка книги",
                        modifier = Modifier.size(200.dp),
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.error),
                        onError = { e ->
                            Log.e("AsyncImage", "Ошибка загрузки: ${e.result.throwable.message}")
                        }
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
}
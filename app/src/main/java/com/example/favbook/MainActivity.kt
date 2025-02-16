package com.example.favbook

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import coil.compose.AsyncImage
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


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

    @Composable
    fun AppNavigator(navController: NavHostController,startDectination: String) {
        NavHost(navController = navController, startDestination = startDectination) {
            composable("auth_screen") { AuthScreen(navController) }
            composable("main_screen") { MainScreen(navController) }
            composable("book_screen") { BookScreen() }
            composable("search_screen") { SearchScreen(navController) }
            composable("add_screen") { AddScreen() }
            // Экран деталей книги

            composable("book_detail_screen/{title}/{coverUrl}") { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                } ?: "Без названия"

                val coverUrl = backStackEntry.arguments?.getString("coverUrl")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                } ?: ""

                BookDetailScreen(title, coverUrl)
            }

        }
    }

    @Composable
    fun AuthScreen(navController: NavHostController) {
        val auth = rememberAuth()
        val email = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val errorMessage = remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Вход / Регистрация", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener {
                            navController.navigate("main_screen") {
                                popUpTo("auth_screen") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { errorMessage.value = it.message }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    auth.createUserWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener {
                            navController.navigate("main_screen") {
                                popUpTo("auth_screen") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { errorMessage.value = it.message }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрироваться")
            }

            Spacer(modifier = Modifier.height(8.dp))

            errorMessage.value?.let {
                Text(text = it, color = Color.Red)
            }
        }
    }

    @Composable
    fun rememberFirebaseUser(): FirebaseUser? {
        val userState = produceState<FirebaseUser?>(initialValue = null) {
            value = FirebaseAuth.getInstance().currentUser
        }
        return userState.value
    }

    @Composable
    fun rememberAuth(): FirebaseAuth {
        return remember { FirebaseAuth.getInstance() }
    }

    //Возможно стоит удалить navController
    @Composable
    fun MainScreen(navController: NavHostController) {
        val auth = FirebaseAuth.getInstance()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Главный экран",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("auth_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }
            ) {
                Text("Выйти")
            }
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

        LaunchedEffect(searchQuery.value) {
            delay(500) // Дебаунс 500 мс
            searchBooksApi(searchQuery.value, books, errorMessage)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Введите название книги") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(books.value) { book ->
                    BookItem(book, navController)
                }
            }
        }
    }


    @Composable
    fun BookItem(book: BookItem, navController: NavHostController) {
        val coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")

        val encodedTitle = URLEncoder.encode(book.volumeInfo.title, StandardCharsets.UTF_8.toString())
        val encodedCoverUrl = URLEncoder.encode(coverUrl ?: "", StandardCharsets.UTF_8.toString())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.LightGray)
                .padding(8.dp)
                .clickable {
                    navController.navigate("book_detail_screen/$encodedTitle/$encodedCoverUrl")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl?.takeIf { it.isNotEmpty() },
                contentDescription = "Обложка книги",
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
                modifier = Modifier.size(50.dp)
            )

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
        val bookDescription = remember { mutableStateOf<String?>(null) }

        // Загружаем описание книги
        LaunchedEffect(title) {
            val response = RetrofitInstance.api.searchBooks(query = title, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
            val bookItem = response.items?.firstOrNull()
            bookDescription.value = bookItem?.volumeInfo?.description
        }

        // Используем LazyColumn для прокрутки
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Обложка книги
                        AsyncImage(
                            model = coverUrl.takeIf { it.isNotEmpty() }, // Использует URL только если он не пустой
                            contentDescription = "Обложка книги",
                            placeholder = painterResource(R.drawable.placeholder),
                            error = painterResource(R.drawable.placeholder), // Заглушка при ошибке загрузки
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Название книги
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Отображение описания книги, если оно есть
                        bookDescription.value?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } ?: run {
                            Text(
                                text = "Описание не доступно",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
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
                //AppNavigator(navController)
            }
        }
    }
}
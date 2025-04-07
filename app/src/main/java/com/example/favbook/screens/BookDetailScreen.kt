package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.favbook.BuildConfig
import com.example.favbook.R
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.network.RetrofitInstance
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun BookDetailScreen(title: String, coverUrl: String) {
    val bookDescription = remember { mutableStateOf<String?>(null) }
    val decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString())
    val user = FirebaseAuth.getInstance().currentUser // Получаем пользователя напрямую
    val db = FirebaseFirestore.getInstance()

    // Загружаем описание, только если пользователь авторизован
    LaunchedEffect(decodedTitle) {
        if (user != null) {
            // Ищем книгу по названию в Firestore
            db.collection("users").document(user.uid).collection("bookLists")
                .whereEqualTo("volumeInfo.title", decodedTitle)
                .get()
                .addOnSuccessListener { result ->
                    val bookItem = result.documents.firstOrNull()?.toObject(BookItem::class.java)
                    val bookId = bookItem?.id // Получаем bookId из Firestore

                    // Если книга найдена в Firestore
                    if (bookId.isNullOrEmpty()) {
                        // Если bookId пустой, грузим описание из Firestore
                        bookDescription.value = bookItem?.volumeInfo?.description ?: "Описание отсутствует"
                    } else {
                        // Если bookId не пустой, грузим описание через API
                        // Запускаем асинхронный запрос внутри корутины
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.api.searchBooks(query = decodedTitle, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
                                val bookFromApi = response.items?.firstOrNull()

                                // Логируем результат из API
                                Log.d("BookDetailScreen", "API response: $bookFromApi")

                                if (bookFromApi != null) {
                                    val description = bookFromApi.volumeInfo?.description
                                    bookDescription.value = description ?: "Описание отсутствует"
                                } else {
                                    bookDescription.value = "Описание отсутствует"
                                }
                            } catch (e: Exception) {
                                bookDescription.value = "Ошибка загрузки описания из API"
                                Log.e("BookDetailScreen", "Error loading description from API", e)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    bookDescription.value = "Ошибка загрузки описания"
                    Log.e("BookDetailScreen", "Error fetching data from Firestore", it)
                }
        } else {
            // Выводим ошибку, если пользователь не найден
            bookDescription.value = "Пользователь не авторизован"
            Log.e("BookDetailScreen", "User not authorized")
        }
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
                        model = coverUrl.takeIf { it.isNotEmpty() },
                        contentDescription = "Обложка книги",
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder), // Заглушка при ошибке загрузки
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Название книги
                    Text(
                        text = decodedTitle,
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

//@Composable
//fun BookDetailScreen(title: String, coverUrl: String) {
//    val bookDescription = remember { mutableStateOf<String?>(null) }
//    val decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString())
//    val user = rememberFirebaseUser()
//    val db = FirebaseFirestore.getInstance()
//
//// Загружаем bookId и описание в зависимости от наличия данных в Firestore
//    LaunchedEffect(decodedTitle) {
//        user?.let {
//            // Ищем книгу по названию в Firestore
//            db.collection("users").document(it.uid).collection("bookLists")
//                .whereEqualTo("volumeInfo.title", decodedTitle)
//                .get()
//                .addOnSuccessListener { result ->
//                    val bookItem = result.documents.firstOrNull()?.toObject(BookItem::class.java)
//                    val bookId = bookItem?.id // Получаем bookId из Firestore
//
//                    if (bookId.isNullOrEmpty()) {
//                        // Если bookId пустой, грузим описание из Firestore
//                        bookDescription.value = bookItem?.volumeInfo?.description ?: "Описание отсутствует"
//                    } else {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            try {
//                                val response = RetrofitInstance.api.searchBooks(query = decodedTitle, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
//                                val bookFromApi = response.items?.firstOrNull()
//                                bookDescription.value = bookFromApi?.volumeInfo?.description ?: "Описание отсутствует"
//                            } catch (e: Exception) {
//                                // Обработка ошибки при запросе
//                                bookDescription.value = "Ошибка загрузки описания из API"
//                            }
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    bookDescription.value = "Ошибка загрузки описания"
//                }
//        }
//    }
//
//
////    // Загружаем описание книги
////    LaunchedEffect(decodedTitle) {
////        val response = RetrofitInstance.api.searchBooks(query = decodedTitle, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
////
////        val bookItem = response.items?.firstOrNull()
////
////        val description = bookItem?.volumeInfo?.description
////        bookDescription.value = bookItem?.volumeInfo?.description
////
////    }
//
//    // Используем LazyColumn для прокрутки
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        contentPadding = PaddingValues(bottom = 16.dp)
//    ) {
//        item {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // Обложка книги
//                    AsyncImage(
//                        model = coverUrl.takeIf { it.isNotEmpty() }, // Использует URL только если он не пустой
//                        contentDescription = "Обложка книги",
//                        placeholder = painterResource(R.drawable.placeholder),
//                        error = painterResource(R.drawable.placeholder), // Заглушка при ошибке загрузки
//                        modifier = Modifier.size(200.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Название книги
//                    Text(
//                        text = decodedTitle,
//                        style = MaterialTheme.typography.titleLarge
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Отображение описания книги, если оно есть
//                    bookDescription.value?.let {
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                    } ?: run {
//                        Text(
//                            text = "Описание не доступно",
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
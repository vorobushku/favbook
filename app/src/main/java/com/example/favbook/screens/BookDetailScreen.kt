package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.favbook.BuildConfig
import com.example.favbook.R
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BookDetailScreen(title: String, coverUrl: String, authors: String, navController: NavController) {
    val decodedAuthors = URLDecoder.decode(authors, StandardCharsets.UTF_8.toString())
    val authorListFromArgs = decodedAuthors.split(",").map { it.trim() }
    val decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString())
    val authorsState = remember { mutableStateOf<List<String>>(authorListFromArgs) }

    val bookDescription = remember { mutableStateOf<String?>(null) }
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(decodedTitle,decodedAuthors) {
        if (user != null) {
            db.collection("users").document(user.uid).collection("bookLists")
                .whereEqualTo("volumeInfo.title", decodedTitle)
                .get()
                .addOnSuccessListener { result ->
                    val bookItem = result.documents.firstOrNull()?.toObject(BookItem::class.java)
                    val bookId = bookItem?.id

                    if (bookId?.startsWith("manual") == true) {
                        bookDescription.value =
                            bookItem.volumeInfo?.description ?: "Описание отсутствует"
                        bookItem?.volumeInfo?.authors?.let {
                            authorsState.value = it
                        }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val query = "$decodedTitle ${authorListFromArgs.joinToString(" ")}"
                                val response = RetrofitInstance.api.searchBooks(
                                    query = query,
                                    apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
                                )
                                val matchedBook = response.items?.firstOrNull { book ->
                                    val titleMatch = book.volumeInfo.title.equals(decodedTitle, ignoreCase = true)
                                    val authorMatch = book.volumeInfo.authors?.any { it in authorListFromArgs } == true
                                    titleMatch && authorMatch
                                } ?: response.items?.firstOrNull()

                                if (matchedBook != null) {
                                    bookDescription.value = matchedBook.volumeInfo.description ?: "Описание отсутствует"
                                    authorsState.value = matchedBook.volumeInfo.authors ?: authorListFromArgs
                                } else {
                                    bookDescription.value = "Описание не найдено"
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

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .graphicsLayer {
                    translationY = -scrollState.value.toFloat() // Уезжает вверх при скролле
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF98968A), Color(0xFF504B4B)),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = decodedTitle,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (authorsState.value.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 25.dp)) {
                        authorsState.value.forEach { author ->
                            Text(
                                text = author,
                                color = Color(0xFFFFD700),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .clickable {
                                        val encodedAuthor = URLEncoder.encode(author, StandardCharsets.UTF_8.toString())
                                        navController.navigate("author_books_screen/$encodedAuthor")
                                    }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Автор неизвестен",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 25.dp)
                    )
                }

                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Обложка книги",
                    placeholder = painterResource(R.drawable.placeholder),
                    error = painterResource(R.drawable.placeholder),
                    modifier = Modifier
                        .size(width = 120.dp, height = 200.dp)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(8.dp))

                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = bookDescription.value ?: "Описание не доступно",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
}
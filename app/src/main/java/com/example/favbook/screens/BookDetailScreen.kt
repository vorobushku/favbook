package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.favbook.AnyBook
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
fun BookDetailScreen(anyBook: AnyBook, navController: NavController) {
    val scrollState = rememberScrollState()

    val bookDescription = remember { mutableStateOf<String?>(null) }
    val authorsState = remember { mutableStateOf<List<String>>(emptyList()) }
    val title: String
    val coverUrl: String?
    val initialAuthors: List<String>

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    when (anyBook) {
        is AnyBook.GoogleBook -> {
            val book = anyBook.book
            title = book.volumeInfo.title
            coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
            initialAuthors = book.volumeInfo.authors ?: emptyList()

            val localDescription = book.volumeInfo.description

            LaunchedEffect(title, initialAuthors) {
                if (user != null) {
                    db.collection("users").document(user.uid).collection("bookLists")
                        .whereEqualTo("volumeInfo.title", title)
                        .get()
                        .addOnSuccessListener { result ->
                            val bookItem = result.documents.firstOrNull()?.toObject(BookItem::class.java)
                            val bookId = bookItem?.id

                            if (bookId?.startsWith("manual") == true) {
                                bookDescription.value = bookItem.volumeInfo.description ?: "Описание отсутствует"
                                authorsState.value = bookItem.volumeInfo.authors ?: initialAuthors
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        if (!localDescription.isNullOrBlank()) {
                                            bookDescription.value = localDescription
                                            authorsState.value = initialAuthors
                                        } else {
                                            bookDescription.value = "Описание не найдено"
                                            authorsState.value = initialAuthors
                                        }
                                    } catch (e: Exception) {
                                        bookDescription.value = "Ошибка загрузки описания из API"
                                        authorsState.value = initialAuthors
                                        Log.e("BookDetailScreen", "API error", e)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            bookDescription.value = "Ошибка загрузки описания"
                            Log.e("BookDetailScreen", "Firestore error", it)
                        }
                } else {
                    bookDescription.value = "Пользователь не авторизован"
                }
            }
        }

        is AnyBook.NYTimesBook -> {
            val book = anyBook.book
            title = book.title
            coverUrl = book.book_image
            initialAuthors = listOf(book.author)
            bookDescription.value = book.description
            authorsState.value = initialAuthors
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    val lists = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(user) {
        user?.let {
            db.collection("users").document(it.uid).collection("bookLists").get()
                .addOnSuccessListener { result ->
                    val availableLists = result.documents
                        .mapNotNull { it.getString("listType") }
                        .filter { it.lowercase() != "добавленные книги" && !it.contains(",") }
                        .distinct()
                    lists.value = availableLists
                }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
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
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (authorsState.value.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 30.dp)) {
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
                        modifier = Modifier.padding(top = 30.dp)
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Обложка книги",
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder),
                        modifier = Modifier
                            .size(width = 120.dp, height = 200.dp)
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .align(Alignment.CenterStart)
                    )

                    Text(
                        text = "...",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { showDialog = true }
                            .padding(end = 20.dp)
                    )
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Добавить книгу в список") },
                        text = {
                            Column {
                                if (lists.value.isEmpty()) {
                                    Text("Сначала необходимо добавить списки")
                                } else {
                                    lists.value.forEach { list ->
                                        Button(
                                            onClick = {
                                                val bookData = convertAnyBookToMap(anyBook) + mapOf("listType" to list)

                                                db.collection("users").document(user!!.uid)
                                                    .collection("bookLists")
                                                    .add(bookData + mapOf("listType" to list))
                                                    .addOnSuccessListener {
                                                        Log.d("Firestore", "Книга успешно добавлена в $list")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("Firestore", "Ошибка при добавлении книги", e)
                                                    }

                                                showDialog = false
                                            },
                                            modifier = Modifier
                                                .padding(vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFF5D79C), // Цвет фона кнопки
                                                contentColor = Color.Black
                                            )
                                        ) {
                                            Text(list)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray, // Цвет фона кнопки
                                )
                            ) {
                                Text("Отмена")
                            }
                        }
                    )
                }

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

fun convertAnyBookToMap(anyBook: AnyBook): Map<String, Any?> {
    return when (anyBook) {
        is AnyBook.GoogleBook -> {
            val book = anyBook.book
            book.toMap()
        }

        is AnyBook.NYTimesBook -> {
            val nyt = anyBook.book
            mapOf(
                "id" to "nyt_${nyt.title.hashCode()}",
                "volumeInfo" to mapOf(
                    "title" to nyt.title,
                    "authors" to listOf(nyt.author),
                    "imageLinks" to mapOf(
                        "thumbnail" to nyt.book_image,
                        "smallThumbnail" to nyt.book_image
                    ),
                    "description" to nyt.description
                )
            )
        }
    }
}
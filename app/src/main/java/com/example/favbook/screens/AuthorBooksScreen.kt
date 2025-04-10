package com.example.favbook.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.example.favbook.BuildConfig
import com.example.favbook.BookItem
import com.example.favbook.data.network.RetrofitInstance
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthorBooksScreen(author: String, navController: NavHostController) {
    val decodedAuthor = URLDecoder.decode(author, StandardCharsets.UTF_8.toString())
    val books = remember { mutableStateOf<List<BookItem>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(decodedAuthor) {
        try {
            val response = RetrofitInstance.api.searchBooks(
                query = "inauthor:$decodedAuthor",
                apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
            )
            val result = response.items.orEmpty()
            if (result.isEmpty()) {
                errorMessage.value = "Книги этого автора не найдены"
            } else {
                books.value = result
            }
        } catch (e: Exception) {
            errorMessage.value = "Ошибка загрузки книг"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Автор: $decodedAuthor",
            style = MaterialTheme.typography.headlineMedium
        )

        if (errorMessage.value != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage.value!!,
                color = Color.Red
            )
        } else if (books.value.isNotEmpty()) {
            LazyColumn {
                items(books.value) { book ->
                    BookItem(book = book, navController = navController, user = FirebaseAuth.getInstance().currentUser)
                }
            }
        }
    }
}
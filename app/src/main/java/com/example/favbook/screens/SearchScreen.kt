package com.example.favbook.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.favbook.data.model.BookItem
import com.example.favbook.rememberFirebaseUser
import kotlinx.coroutines.delay
import com.example.favbook.BuildConfig
import com.example.favbook.data.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.favbook.BookItem

@Composable
fun SearchScreen(navController: NavHostController) {
    val user = rememberFirebaseUser()
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
        Text(
            text = "Поиск книги",
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 15.dp)
            ,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Введите название книги") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(books.value) { book ->
                BookItem(book, navController, user)
            }
        }
    }
}

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
            val books: List<BookItem> = response.items ?: emptyList()

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
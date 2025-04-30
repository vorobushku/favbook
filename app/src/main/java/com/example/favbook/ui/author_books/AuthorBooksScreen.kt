package com.example.favbook.ui.author_books

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
import androidx.navigation.NavHostController
import com.example.favbook.ui.book.BookItemCard
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthorBooksScreen(
    author: String,
    navController: NavHostController,
    viewModel: AuthorBooksViewModel = hiltViewModel()
) {
    val decodedAuthor = URLDecoder.decode(author, StandardCharsets.UTF_8.toString())

    LaunchedEffect(decodedAuthor) {
        viewModel.loadBooksByAuthor(decodedAuthor)
    }

    val books = viewModel.books.value
    val errorMessage = viewModel.errorMessage.value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = decodedAuthor,
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 15.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = Color.Red
            )
        } else if (books.isNotEmpty()) {
            LazyColumn {
                items(books) { book ->
                    BookItemCard(book = book, navController = navController, user = FirebaseAuth.getInstance().currentUser)
                }
            }
        }
    }
}
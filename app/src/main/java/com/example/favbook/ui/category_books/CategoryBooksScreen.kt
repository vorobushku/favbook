package com.example.favbook.ui.category_books

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.favbook.data.firebase.rememberFirebaseUser
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.favbook.ui.book.BookItemCard


@Composable
fun CategoryBooksScreen(category: String, navController: NavHostController) {
    val viewModel: CategoryBooksViewModel = hiltViewModel()
    val user = rememberFirebaseUser()
    val books = viewModel.books.value

    LaunchedEffect(user, category) {
        user?.uid?.let { uid ->
            viewModel.loadBooksForCategory(uid, category)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        Text(
            text = category,
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 15.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        LazyColumn {
            items(books) { book ->
                BookItemCard(
                    book = book,
                    navController = navController,
                    user = user,
                    onBookDeleted = {
                        viewModel.removeBookFromList(book.id)
                    }
                )
            }
        }
    }
}

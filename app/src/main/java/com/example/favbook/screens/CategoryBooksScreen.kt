package com.example.favbook.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.example.favbook.data.model.BookItem
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import com.example.favbook.BookItem


@Composable
fun CategoryBooksScreen(category: String, navController: NavHostController) {
    val user = rememberFirebaseUser()
    val books = remember { mutableStateOf<List<BookItem>>(emptyList()) }

    LaunchedEffect(user, category) {
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("bookLists")
                .whereEqualTo("listType", category)
                .get()
                .addOnSuccessListener { result ->
                    val bookList = result.documents.mapNotNull { it.toObject(BookItem::class.java) }
                    books.value = bookList
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Категория: $category", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(books.value) { book ->
                BookItem(book, navController, user)
            }
        }
    }
}
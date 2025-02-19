package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favbook.data.model.BookItem
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.favbook.BookItemView

@Composable
fun BookScreen() {
    val user = rememberFirebaseUser()
    val books = remember { mutableStateOf<List<BookItem>>(emptyList()) }

    LaunchedEffect(user) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).collection("bookLists")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    books.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(BookItem::class.java) // Преобразуем Firestore документ в объект BookItem
                    }
                }
        }
    }


    // Проверяем, что приходит в books.value
    LaunchedEffect(books.value) {
        Log.d("BookScreen", "Books loaded: ${books.value.size}")
        books.value.forEach {
            Log.d("BookScreen", "Book title: ${it.volumeInfo.title}")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(books.value) { book ->
            // Используем данные из BookItem для отображения
            val title = book.volumeInfo.title ?: "Без названия"
            // Кодируем и заменяем http на https в URL изображения
            val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.let {
                it.replace("http://", "https://")
            } ?: ""


            // Проверим, что данные приходят
            Log.d("BookItem", "Title: $title, Image URL: $imageUrl")

            // Передаем данные в BookItem для отображения
            BookItemView(title = title, imageUrl = imageUrl)
        }
    }
}
package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.favbook.data.model.BookItem
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.favbook.BookItemView

@Composable
fun BookScreen(navController: NavController) { // Добавляем navController
    val user = rememberFirebaseUser()
    val booksByList = remember { mutableStateOf<Map<String, List<BookItem>>>(emptyMap()) }

    LaunchedEffect(user) {
        user?.let {
            loadUserBooks(it.uid) { groupedBooks ->
                booksByList.value = groupedBooks
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        booksByList.value.keys.forEach { listType ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("bookListScreen/$listType") // Переход на экран списка книг
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = listType,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Divider()
                }
            }
        }
    }
}

// Функция загрузки книг пользователя из Firestore
fun loadUserBooks(userId: String, onResult: (Map<String, List<BookItem>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).collection("bookLists")
        .get()
        .addOnSuccessListener { result ->
            val groupedBooks = result.documents.mapNotNull { doc ->
                doc.toObject(BookItem::class.java)?.let { book ->
                    val listType = doc.getString("listType") ?: "Без категории"
                    listType to book
                }
            }.groupBy({ it.first }, { it.second })

            onResult(groupedBooks)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Ошибка загрузки книг: ", e)
        }
}


//@Composable
//fun BookScreen() {
//    val user = rememberFirebaseUser()
//    val books = remember { mutableStateOf<List<BookItem>>(emptyList()) }
//
//    LaunchedEffect(user) {
//        if (user != null) {
//            val db = FirebaseFirestore.getInstance()
//            db.collection("users").document(user.uid).collection("bookLists")
//                .addSnapshotListener { snapshot, e ->
//                    if (e != null || snapshot == null) return@addSnapshotListener
//                    books.value = snapshot.documents.mapNotNull { doc ->
//                        doc.toObject(BookItem::class.java) // Преобразуем Firestore документ в объект BookItem
//                    }
//                }
//        }
//    }
//
//
//    // Проверяем, что приходит в books.value
//    LaunchedEffect(books.value) {
//        Log.d("BookScreen", "Books loaded: ${books.value.size}")
//        books.value.forEach {
//            Log.d("BookScreen", "Book title: ${it.volumeInfo.title}")
//        }
//    }
//
//    LazyColumn(
//        modifier = Modifier.fillMaxSize().padding(16.dp)
//    ) {
//        items(books.value) { book ->
//            // Используем данные из BookItem для отображения
//            val title = book.volumeInfo.title ?: "Без названия"
//            // Кодируем и заменяем http на https в URL изображения
//            val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.let {
//                it.replace("http://", "https://")
//            } ?: ""
//
//
//            // Проверим, что данные приходят
//            Log.d("BookItem", "Title: $title, Image URL: $imageUrl")
//
//            // Передаем данные в BookItem для отображения
//            BookItemView(title = title, imageUrl = imageUrl)
//        }
//    }
//}
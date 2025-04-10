package com.example.favbook

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BookItem(book: BookItem, navController: NavHostController, user: FirebaseUser?) {
    val coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
    val encodedTitle = URLEncoder.encode(book.volumeInfo.title, StandardCharsets.UTF_8.toString())
    val encodedCoverUrl = URLEncoder.encode(coverUrl ?: "", StandardCharsets.UTF_8.toString())
    val encodedAuthors = URLEncoder.encode(book.volumeInfo.authors?.joinToString(",") ?: "", StandardCharsets.UTF_8.toString())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray)
            .padding(8.dp)
            .clickable {
                navController.navigate("book_detail_screen/$encodedTitle/$encodedCoverUrl/$encodedAuthors")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coverUrl?.takeIf { it.isNotEmpty() },
            contentDescription = "Обложка книги",
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            modifier = Modifier.size(50.dp)
        )

        Column (
            modifier = Modifier.weight(1f)
        )
        {
            Text(
                text = "Название: ${book.volumeInfo.title}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Авторы: ${book.volumeInfo.authors?.joinToString(", ") ?: "Неизвестно"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Кнопка добавления в список
        if (user != null) {
            var showDialog by remember { mutableStateOf(false) }

            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Добавить книгу в список") },
                    text = {
                        Column {
                            Button(onClick = {
                                addBookToUserList(user.uid, book, "Хочу прочитать")
                                showDialog = false
                            }) {
                                Text("Хочу прочитать")
                            }
                            Button(onClick = {
                                addBookToUserList(user.uid, book, "Читаю")
                                showDialog = false
                            }) {
                                Text("Читаю")
                            }
                            Button(onClick = {
                                addBookToUserList(user.uid, book, "Прочитано")
                                showDialog = false
                            }) {
                                Text("Прочитано")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

fun addBookToUserList(userId: String, book: BookItem, listType: String) {
    val db = FirebaseFirestore.getInstance()
    val userBooksRef = db.collection("users").document(userId).collection("bookLists")

    val bookData = book.toMap() + mapOf("listType" to listType)

    userBooksRef.add(bookData)
        .addOnSuccessListener {
            Log.d("Firestore", "Book added to user list successfully!")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error adding book to user list", e)
        }
}
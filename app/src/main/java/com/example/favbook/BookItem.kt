package com.example.favbook

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val encodedAuthors = URLEncoder.encode(
        book.volumeInfo.authors?.joinToString(",") ?: "",
        StandardCharsets.UTF_8.toString()
    )

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "book",
                    AnyBook.GoogleBook(book)
                )
                navController.navigate(Screen.BookDetail.route)
            },
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl?.takeIf { it.isNotEmpty() },
                contentDescription = "Обложка книги",
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
                modifier = Modifier.size(60.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            )
            {
                Text(
                    text = book.volumeInfo.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.volumeInfo.authors?.joinToString(", ") ?: "Неизвестно",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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
                                if (lists.value.isEmpty()) {
                                    Text("Сначала необходимо добавить списки")
                                } else {
                                    lists.value.forEach { list ->
                                        Button(
                                            onClick = {
                                                addBookToUserList(user.uid, book, list)
                                                showDialog = false
                                            },
                                            modifier = Modifier.padding(horizontal = 2.dp),
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) {
                                Text("Отмена")
                            }
                        }
                    )
                }

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
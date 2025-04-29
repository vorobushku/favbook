package com.example.favbook

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookItem(book: BookItem, navController: NavHostController, user: FirebaseUser?,onBookDeleted: (() -> Unit)? = null) {
    val coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
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
                var expanded by remember { mutableStateOf(false) }
                var userBookDocId by remember { mutableStateOf<String?>(null) }
                var currentListType by remember { mutableStateOf<String?>(null) }

                // Проверяем, есть ли книга в Firestore
                LaunchedEffect(book.id, user.uid) {
                    val userBooksRef = db.collection("users").document(user.uid).collection("bookLists")
                    userBooksRef
                        .whereEqualTo("id", book.id)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                val doc = result.documents.first()
                                userBookDocId = doc.id
                                currentListType = doc.getString("listType")
                            } else {
                                userBookDocId = null
                                currentListType = null
                            }
                        }
                }

                BookOptionsMenu(
                    book = book,
                    user = user,
                    icon = {
                        Icon(Icons.Default.MoreVert, contentDescription = "Опции")
                    },
                    onBookDeleted = onBookDeleted
                )

//                Box {
//                    var showMenuDialog by remember { mutableStateOf(false) }
//
//                    IconButton(onClick = { showMenuDialog = true }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "Опции")
//                    }
//
//                    if (showMenuDialog) {
//                        AlertDialog(
//                            onDismissRequest = { showMenuDialog = false },
//                            title = { Text(text = if (userBookDocId == null) "Добавить книгу в список" else "Управление книгой") },
//                            text = {
//                                Column {
//                                    if (userBookDocId == null) {
//                                        if (lists.value.isEmpty()) {
//                                            Text("Сначала необходимо добавить списки")
//                                        } else {
//                                            lists.value.forEach { list ->
//                                                PrimaryStyledButton(text = list) {
//                                                    addBookToUserList(user.uid, book, list)
//                                                    showMenuDialog = false
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        PrimaryStyledButton(text = "Удалить из списка") {
//                                            deleteBookFromUserList(user.uid, userBookDocId!!)
//                                            onBookDeleted?.invoke()
//                                            showMenuDialog = false
//                                        }
//                                        val otherLists = lists.value.filter { it != currentListType }
//                                        if (otherLists.isNotEmpty()) {
//                                            Text("Переместить в категорию: ", style = MaterialTheme
//                                                .typography.titleLarge, color = Color.Black, modifier = Modifier.padding(7.dp))
//                                            otherLists.forEach { list ->
//                                                PrimaryStyledButton(list) {
//                                                    updateBookCategory(user.uid, userBookDocId!!, list)
//                                                    currentListType = list
//                                                    onBookDeleted?.invoke()
//                                                    showMenuDialog = false
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            },
//                            confirmButton = {
//                                CancelStyledButton("Отмена") {showMenuDialog = false}
//                            }
//                        )
//                    }
//                }
            }
        }
    }
}

@Composable
fun CancelStyledButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Gray,
        ),
        modifier = Modifier
            .padding(vertical = 2.dp)
    ) {
        Text(text)
    }
}

@Composable
fun PrimaryStyledButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF5D79C),
            contentColor = Color.Black
        ),
        modifier = Modifier
            .padding(vertical = 2.dp)
    ) {
        Text(text)
    }
}

@Composable
fun AuthStyledButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(60),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF5D79C),
            contentColor = Color.Black
        ),
        modifier = modifier
            .padding(vertical = 2.dp)
            .size(60.dp)
    ) {
        Text(text)
    }
}

fun deleteBookFromUserList(userId: String, documentId: String) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).collection("bookLists").document(documentId)
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Book deleted from list")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error deleting book", e)
        }
}

fun updateBookCategory(userId: String, documentId: String, newList: String) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).collection("bookLists").document(documentId)
        .update("listType", newList)
        .addOnSuccessListener {
            Log.d("Firestore", "Book moved to $newList")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error moving book", e)
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


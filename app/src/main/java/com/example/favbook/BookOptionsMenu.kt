package com.example.favbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookOptionsMenu(
    book: BookItem,
    user: FirebaseUser,
    icon: @Composable (() -> Unit),
    onBookDeleted: (() -> Unit)? = null
) {
    val db = FirebaseFirestore.getInstance()
    val lists = remember { mutableStateOf<List<String>>(emptyList()) }
    var userBookDocId by remember { mutableStateOf<String?>(null) }
    var currentListType by remember { mutableStateOf<String?>(null) }
    var showMenuDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showMenuDialog) {
        if (showMenuDialog) {
            val dbRef = db.collection("users").document(user.uid).collection("bookLists")
            dbRef.get()
                .addOnSuccessListener { result ->
                    val availableLists = result.documents
                        .mapNotNull { it.getString("listType") }
                        .filter { it.lowercase() != "добавленные книги" && !it.contains(",") }
                        .distinct()
                    lists.value = availableLists
                }

            dbRef.whereEqualTo("id", book.id)
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
    }

    Box() {
        Box(modifier = Modifier.clickable { showMenuDialog = true }) {
            icon()
        }

        if (showMenuDialog) {
            AlertDialog(
                onDismissRequest = { showMenuDialog = false },
                title = {
                    Text(if (userBookDocId == null) "Добавить книгу в список" else "Управление книгой")
                },
                text = {
                    Column {
                        if (userBookDocId == null) {
                            if (lists.value.isEmpty()) {
                                Text("Сначала необходимо добавить списки")
                            } else {
                                lists.value.forEach { list ->
                                    PrimaryStyledButton(text = list) {
                                        addBookToUserList(user.uid, book, list)
                                        showMenuDialog = false
                                    }
                                }
                            }
                        } else {
                            PrimaryStyledButton(text = "Удалить из списка") {
                                deleteBookFromUserList(user.uid, userBookDocId!!)
                                onBookDeleted?.invoke()
                                showMenuDialog = false
                            }
                            val otherLists = lists.value.filter { it != currentListType }
                            if (otherLists.isNotEmpty()) {
                                Text("Переместить в категорию:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                                otherLists.forEach { list ->
                                    PrimaryStyledButton(list) {
                                        updateBookCategory(user.uid, userBookDocId!!, list)
                                        currentListType = list
                                        onBookDeleted?.invoke()
                                        showMenuDialog = false
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    CancelStyledButton("Отмена") {
                        showMenuDialog = false
                    }
                }
            )
        }
    }
}
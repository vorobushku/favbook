package com.example.favbook.ui.book

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseUser

@Composable
fun BookOptionsDialog(
    book: BookItem,
    user: FirebaseUser,
    viewModel: BookItemViewModel,
    onDismiss: () -> Unit,
    onBookDeleted: () -> Unit
) {
    val lists by viewModel.lists
    val docId by viewModel.userBookDocId
    val currentList by viewModel.currentListType

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (docId == null) "Добавить книгу в список" else "Управление книгой")
        },
        text = {
            Column {
                if (docId == null) {
                    if (lists.isEmpty()) {
                        Text("Сначала необходимо добавить списки")
                    } else {
                        lists.forEach { list ->
                            PrimaryStyledButton(list) {
                                viewModel.addBookToList(user.uid, book, list, onDismiss)
                            }
                        }
                    }
                } else {
                    PrimaryStyledButton("Удалить из списка") {
                        viewModel.deleteBook(user.uid) {
                            onBookDeleted()
                        }
                    }
                    val otherLists = lists.filter { it != currentList }
                    if (otherLists.isNotEmpty()) {
                        Text("Переместить в категорию:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        otherLists.forEach { list ->
                            PrimaryStyledButton(list) {
                                viewModel.moveBook(user.uid, list) {
                                    onBookDeleted()
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            CancelStyledButton("Отмена", onDismiss)
        }
    )
}
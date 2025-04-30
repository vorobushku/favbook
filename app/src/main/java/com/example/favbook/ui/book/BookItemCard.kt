package com.example.favbook.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.favbook.R
import com.example.favbook.navigation.Screen
import com.example.favbook.data.model.AnyBook
import com.example.favbook.data.model.BookItem
import com.google.firebase.auth.FirebaseUser

@Composable
fun BookItemCard(
    book: BookItem,
    navController: NavHostController,
    user: FirebaseUser?,
    viewModel: BookItemViewModel = hiltViewModel(),
    onBookDeleted: (() -> Unit)? = null
) {
    val coverUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")

    val showMenuDialog = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                navController.currentBackStackEntry?.savedStateHandle?.set("book",
                    AnyBook.GoogleBook(book)
                )
                navController.navigate(Screen.BookDetail.route)
            },
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl?.takeIf { it.isNotEmpty() },
                contentDescription = "Обложка книги",
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
                modifier = Modifier.size(60.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(book.volumeInfo.title, style = MaterialTheme.typography.titleMedium)
                Text(book.volumeInfo.authors?.joinToString(", ") ?: "Неизвестно", style = MaterialTheme.typography.bodyMedium)
            }

            if (user != null) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Опции",
                    modifier = Modifier.clickable {
                        viewModel.loadListsAndBookInfo(user.uid, book.id)
                        showMenuDialog.value = true
                    }
                )

                if (showMenuDialog.value) {
                    BookOptionsDialog(
                        book = book,
                        user = user,
                        viewModel = viewModel,
                        onDismiss = { showMenuDialog.value = false },
                        onBookDeleted = {
                            onBookDeleted?.invoke()
                            showMenuDialog.value = false
                        }
                    )
                }
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


package com.example.favbook.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.favbook.data.model.AnyBook
import com.example.favbook.ui.book.PrimaryStyledButton
import com.example.favbook.navigation.Screen
import com.example.favbook.data.model.NYTBook

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Главный экран",
            modifier = Modifier
                .padding(top = 30.dp, start = 11.dp),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryStyledButton(text = "Выйти") {
            viewModel.logout {
                navController.navigate("auth_screen") {
                    popUpTo("main_screen") { inclusive = true }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("🔥 Топ книг NYTimes", style = MaterialTheme.typography.titleMedium)

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyRow {
                items(state.topBooks) { book ->
                    NYTBookCard(book = book, navController = navController)
                }
            }
        }

        state.errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun NYTBookCard(book: NYTBook, navController: NavHostController) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable {
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "book",
                    AnyBook.NYTimesBook(book)
                )
                navController.navigate(Screen.BookDetail.route)
            }
    ) {
        AsyncImage(
            model = book.book_image,
            contentDescription = "Обложка книги",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(book.title, maxLines = 2, style = MaterialTheme.typography.titleSmall)
        Text(book.author, style = MaterialTheme.typography.bodySmall)
    }
}
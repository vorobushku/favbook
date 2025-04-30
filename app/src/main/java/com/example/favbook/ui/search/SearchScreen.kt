package com.example.favbook.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.favbook.data.firebase.rememberFirebaseUser
import com.example.favbook.ui.book.BookItemCard
import com.example.favbook.data.model.Resource
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val user = rememberFirebaseUser()
    val queryState = viewModel.searchQuery.collectAsState()
    val booksState = viewModel.books.collectAsState()

    // Дебаунс при вводе
    LaunchedEffect(queryState.value) {
        delay(500)
        if (queryState.value.isNotBlank()) {
            viewModel.searchBooks(queryState.value)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Поиск книги",
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 15.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = queryState.value,
            onValueChange = { viewModel.updateQuery(it) },
            label = { Text("Введите название книги") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val result = booksState.value) {
            is Resource.Loading -> {
                CircularProgressIndicator()
            }
            is Resource.Success -> {
                LazyColumn {
                    items(result.data) { book ->
                        BookItemCard(book, navController, user)
                    }
                }
            }
            is Resource.Error -> {
                Text(text = result.message ?: "Неизвестная ошибка")
            }
        }
    }
}
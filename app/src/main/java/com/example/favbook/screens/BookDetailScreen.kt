package com.example.favbook.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.favbook.BuildConfig
import com.example.favbook.R
import com.example.favbook.data.network.RetrofitInstance
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun BookDetailScreen(title: String, coverUrl: String) {
    val bookDescription = remember { mutableStateOf<String?>(null) }
    val decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.toString())

    // Загружаем описание книги
    LaunchedEffect(decodedTitle) {
        val response = RetrofitInstance.api.searchBooks(query = decodedTitle, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
        val bookItem = response.items?.firstOrNull()
        bookDescription.value = bookItem?.volumeInfo?.description
    }

    // Используем LazyColumn для прокрутки
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Обложка книги
                    AsyncImage(
                        model = coverUrl.takeIf { it.isNotEmpty() }, // Использует URL только если он не пустой
                        contentDescription = "Обложка книги",
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder), // Заглушка при ошибке загрузки
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Название книги
                    Text(
                        text = decodedTitle,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Отображение описания книги, если оно есть
                    bookDescription.value?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } ?: run {
                        Text(
                            text = "Описание не доступно",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
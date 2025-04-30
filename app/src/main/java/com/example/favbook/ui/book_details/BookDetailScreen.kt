package com.example.favbook.ui.book_details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.favbook.data.model.AnyBook
import com.example.favbook.ui.book.BookItemViewModel
import com.example.favbook.ui.book.BookOptionsDialog
//import com.example.favbook.BookOptionsMenu
import com.example.favbook.R
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.model.ImageLinks
import com.example.favbook.data.model.VolumeInfo
//import com.example.favbook.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BookDetailScreen(
    anyBook: AnyBook,
    navController: NavController,
    viewModel: BookDetailViewModel = hiltViewModel(),
    bookItemViewModel: BookItemViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val description by viewModel.description
    val authors by viewModel.authors
    val user = FirebaseAuth.getInstance().currentUser
    val showMenuDialog = remember { mutableStateOf(false) }

    val title: String
    val coverUrl: String?

    LaunchedEffect(anyBook) {
        viewModel.loadBookDetails(anyBook)
        viewModel.loadAvailableLists()
    }

    when (anyBook) {
        is AnyBook.GoogleBook -> {
            title = anyBook.book.volumeInfo.title
            coverUrl = anyBook.book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
        }

        is AnyBook.NYTimesBook -> {
            title = anyBook.book.title
            coverUrl = anyBook.book.book_image
        }
    }

    val bookItem = convertAnyBookToBookItem(anyBook)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .graphicsLayer { translationY = -scrollState.value.toFloat() }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF98968A), Color(0xFF504B4B)),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (authors.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 30.dp)) {
                        authors.forEach { author ->
                            Text(
                                text = author,
                                color = Color(0xFFFFD700),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.clickable {
                                    val encoded = URLEncoder.encode(author, StandardCharsets.UTF_8.toString())
                                    navController.navigate("author_books_screen/$encoded")
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Автор неизвестен",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 30.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Обложка книги",
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder),
                        modifier = Modifier
                            .size(width = 120.dp, height = 200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(horizontal = 3.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (user != null) {
                        Text(
                            text = "...",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            ),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    bookItemViewModel.loadListsAndBookInfo(user.uid, bookItem.id)
                                    showMenuDialog.value = true
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = description ?: "Описание не доступно",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }

        if (showMenuDialog.value && user != null) {
            BookOptionsDialog(
                book = bookItem,
                user = user,
                viewModel = bookItemViewModel,
                onDismiss = { showMenuDialog.value = false },
                onBookDeleted = { showMenuDialog.value = false }
            )
        }
    }
}

fun convertAnyBookToBookItem(anyBook: AnyBook): BookItem {
    return when (anyBook) {
        is AnyBook.GoogleBook -> anyBook.book
        is AnyBook.NYTimesBook -> {
            val nyt = anyBook.book
            BookItem(
                id = "nyt_${nyt.title.hashCode()}",
                volumeInfo = VolumeInfo(
                    title = nyt.title,
                    authors = listOf(nyt.author),
                    imageLinks = ImageLinks(
                        thumbnail = nyt.book_image,
                        smallThumbnail = nyt.book_image
                    ),
                    description = nyt.description
                )
            )
        }
    }
}
package com.example.favbook.screens

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.favbook.AnyBook
import com.example.favbook.PrimaryStyledButton
import com.example.favbook.Screen
import com.example.favbook.data.model.NYTBook
import com.example.favbook.data.network.NYTimesRetrofitInstance
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val user = rememberFirebaseUser()
    val coroutineScope = rememberCoroutineScope()
    var topBooks by remember { mutableStateOf<List<NYTBook>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = NYTimesRetrofitInstance.api.getTopBooks()
                topBooks = response.results.books
            } catch (e: Exception) {
                Log.e("NYT_API", "Ошибка загрузки: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Главный экран",
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 11.dp)
            ,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

//        Button(
//            onClick = {
//                auth.signOut()
//                navController.navigate("auth_screen") {
//                    popUpTo("main_screen") { inclusive = true }
//                }
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFFF5D79C), // Цвет фона кнопки
//                contentColor = Color.Black
//            )
//        ) {
//            Text("Выйти")
//        }

        PrimaryStyledButton(text = "Выйти") {
            auth.signOut()
            navController.navigate("auth_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("🔥 Топ книг NYTimes", style = MaterialTheme.typography.titleMedium)

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyRow {
                items(topBooks) { book ->
                    NYTBookCard(book = book, navController = navController)
                }
            }
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
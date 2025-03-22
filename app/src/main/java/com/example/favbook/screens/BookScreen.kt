package com.example.favbook.screens

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.favbook.data.model.BookItem
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.favbook.BookItemView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//@Composable
//fun BookScreen(navController: NavController) { // Добавляем navController
//    val user = rememberFirebaseUser()
//    val booksByList = remember { mutableStateOf<Map<String, List<BookItem>>>(emptyMap()) }
//
//    LaunchedEffect(user) {
//        user?.let {
//            loadUserBooks(it.uid) { groupedBooks ->
//                booksByList.value = groupedBooks
//            }
//        }
//    }
//
//    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        booksByList.value.keys.forEach { listType ->
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            navController.navigate("bookListScreen/$listType") // Переход на экран списка книг
//                        }
//                        .padding(vertical = 8.dp)
//                ) {
//                    Text(
//                        text = listType,
//                        style = MaterialTheme.typography.titleLarge,
//                        modifier = Modifier.padding(8.dp)
//                    )
//                    Divider()
//                }
//            }
//        }
//    }
//}
//
//// Функция загрузки книг пользователя из Firestore
//fun loadUserBooks(userId: String, onResult: (Map<String, List<BookItem>>) -> Unit) {
//    val db = FirebaseFirestore.getInstance()
//    db.collection("users").document(userId).collection("bookLists")
//        .get()
//        .addOnSuccessListener { result ->
//            val groupedBooks = result.documents.mapNotNull { doc ->
//                doc.toObject(BookItem::class.java)?.let { book ->
//                    val listType = doc.getString("listType") ?: "Без категории"
//                    listType to book
//                }
//            }.groupBy({ it.first }, { it.second })
//
//            onResult(groupedBooks)
//        }
//        .addOnFailureListener { e ->
//            Log.e("Firestore", "Ошибка загрузки книг: ", e)
//        }
//}


@Composable
fun BookScreen(navController: NavHostController) {
    val user = rememberFirebaseUser()
    val categories = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(user) {
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("bookLists")
                .get()
                .addOnSuccessListener { result ->
                    val uniqueCategories = result.documents.mapNotNull { it.getString("listType") }.distinct()
                    categories.value = uniqueCategories
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Ваши списки", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(categories.value) { category ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            val encodedCategory = Uri.encode(category)
                            navController.navigate("category_books_screen/$encodedCategory")
                        }
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(text = category, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
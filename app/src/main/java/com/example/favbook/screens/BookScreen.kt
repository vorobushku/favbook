package com.example.favbook.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

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
                    val allCategories = result.documents
                        .mapNotNull { it.getString("listType") }
                        .flatMap { it.split(",").map { category -> category.trim() } }
                        .distinct()

                    categories.value = allCategories
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
                            navController.navigate("category_books_screen/${URLEncoder.encode(category, "UTF-8")}")
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
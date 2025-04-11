package com.example.favbook.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

@Composable
fun BookScreen(navController: NavHostController) {
    val user = rememberFirebaseUser()
    val categories = remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            loadCategories(user.uid, categories)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategory.isNotBlank()) {
                        addCategoryToFirestore(user?.uid, newCategory.trim(), context) {
                            loadCategories(user!!.uid, categories)
                        }
                        newCategory = ""
                        showDialog = false
                    }
                }) {
                    Text("Добавить", color = Color(0xFF494848))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена",color = Color(0xFF494848))
                }
            },
            title = { Text("Новая категория", style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold)) },

            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("Название категории") },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                        unfocusedLabelColor = Color(0xFF807D7D)
                    )
                )
            },
            shape = RoundedCornerShape(35.dp),

        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Book Lists",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        LazyColumn {
            items(categories.value) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable {
                            navController.navigate("category_books_screen/${URLEncoder.encode(category, "UTF-8")}")
                        },
                    shape = RoundedCornerShape(30.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8BD))
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(25.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }

//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//
//        Text(
//            text = "Book Lists",
//            modifier = Modifier
//                .padding(top = 30.dp)
//                .padding(horizontal = 15.dp)
//            ,
//            style = MaterialTheme.typography.headlineLarge.copy(
//                fontWeight = FontWeight.Bold
//            )
//        )
//
//
//        Spacer(modifier = Modifier.height(30.dp))
//
//        LazyColumn {
//            items(categories.value) { category ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 8.dp, vertical = 6.dp)
//                        .clickable {
//                            navController.navigate("category_books_screen/${URLEncoder.encode(category, "UTF-8")}")
//                        },
//                    shape = RoundedCornerShape(30.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8BD))
//
//                ) {
//                    Text(
//                        text = category,
//                        modifier = Modifier
//                            .padding(25.dp)
//                        ,
//                        textAlign = TextAlign.Center,
//                        style = MaterialTheme.typography.titleLarge.copy(
//                        )
//                    )
//                }
//            }
//        }
//    }
}

private fun loadCategories(userId: String, categories: MutableState<List<String>>) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
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

private fun addCategoryToFirestore(
    userId: String?,
    category: String,
    context: Context,
    onComplete: () -> Unit
) {
    if (userId == null) return

    val db = FirebaseFirestore.getInstance()
    val userListsRef = db.collection("users").document(userId).collection("bookLists")

    userListsRef.get().addOnSuccessListener { result ->
        val existingCategories = result.documents
            .mapNotNull { it.getString("listType") }
            .flatMap { it.split(",").map { it.trim() } }
            .map { it.lowercase() }

        if (category.lowercase() in existingCategories) {
            Toast.makeText(
                context,
                "Категория \"$category\" уже существует",
                Toast.LENGTH_SHORT
            ).show()
            return@addOnSuccessListener
        }

        val newCategoryDoc = mapOf(
            "listType" to category,
            "id" to "template_${category.lowercase()}"
        )

        userListsRef.add(newCategoryDoc)
            .addOnSuccessListener {
                Log.d("Firestore", "Категория успешно добавлена")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Ошибка при добавлении категории", e)
            }

    }.addOnFailureListener { e ->
        Log.e("Firestore", "Ошибка при получении категорий", e)
    }
}



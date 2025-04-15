package com.example.favbook.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.favbook.R
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.model.VolumeInfo
import com.example.favbook.rememberFirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun AddScreen(navController: NavHostController) {
    val user = rememberFirebaseUser()
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val placeholderImage = painterResource(R.drawable.placeholder)
    val lists = remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedList by remember { mutableStateOf<String?>(null) }
    var author by remember { mutableStateOf("") }

    // Получаем списки пользователя из Firestore
    LaunchedEffect(user) {
        user?.let {
            db.collection("users").document(it.uid).collection("bookLists").get()
                .addOnSuccessListener { result ->
                    val availableLists = result.documents
                        .mapNotNull { it.getString("listType") }
                        .filter { !it.contains(",") }
                        .distinct()

                    lists.value = availableLists
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Text("Добавить книгу", style = MaterialTheme.typography.titleLarge)

        Text(
            text = "Добавить книгу",
            modifier = Modifier
                .padding(top = 30.dp)
                .padding(horizontal = 11.dp)
            ,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название книги") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Автор") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.LightGray, focusedLabelColor = Color(0xFF494848),
                unfocusedLabelColor = Color(0xFF807D7D)
            )
        )

//        Image(
//            painter = placeholderImage,
//            contentDescription = "Обложка книги",
//            modifier = Modifier
//                .size(100.dp)
//                .clip(RoundedCornerShape(8.dp))
//        )

        Text("Выберите список", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))


        if (lists.value.isEmpty()) {
            Text(
                text = "Сначала необходимо добавить списки",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            lists.value.forEach { list ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedList = list }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedList == list,
                        onClick = { selectedList = list }
                    )
                    Text(text = list, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Button(
            onClick = {
                user?.let {
                    val bookItem = BookItem(
                        id = "manual",
                        volumeInfo = VolumeInfo(
                            title = title,
                            authors = listOf(author),
                            description = description.takeIf { it.isNotEmpty() } ?: "Описание отсутствует",
                            imageLinks = null
                        )
                    )
                    addBookToUserLibrary(it.uid, bookItem, selectedList)
                    navController.popBackStack()
                }
            },
            enabled = title.isNotEmpty() && selectedList != null
        ) {
            Text("Добавить книгу")
        }
    }
}

fun addBookToUserLibrary(userId: String, book: BookItem, selectedList: String?) {
    val db = FirebaseFirestore.getInstance()
    val userBooksRef = db.collection("users").document(userId).collection("bookLists")

    val listType = listOfNotNull(selectedList, "Добавленные книги").joinToString(", ")

    val bookData = book.toMap() + mapOf(
        "listType" to listType,
        "author" to (book.volumeInfo.authors?.firstOrNull() ?: ""),
        "coverUrl" to (book.volumeInfo.imageLinks?.thumbnail ?: "")
    )

    userBooksRef.add(bookData)
        .addOnSuccessListener {
            Log.d("Firestore", "Книга успешно добавлена с категориями: $listType")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Ошибка при добавлении книги", e)
        }
}

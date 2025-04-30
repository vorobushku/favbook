package com.example.favbook.ui.book

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    var uiState by mutableStateOf(BookUiState())
        private set

    fun loadCategories(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("bookLists")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.documents
                    .mapNotNull { it.getString("listType") }
                    .flatMap { it.split(",").map(String::trim) }
                    .distinct()
                uiState = uiState.copy(categories = categories)
            }
    }

    fun onNewCategoryChange(value: String) {
        uiState = uiState.copy(newCategory = value)
    }

    fun showDialog(show: Boolean) {
        uiState = uiState.copy(showDialog = show)
    }

    fun addCategory(userId: String, context: Context) {
        val category = uiState.newCategory.trim()
        if (category.isBlank()) return

        firestore.collection("users")
            .document(userId)
            .collection("bookLists")
            .get()
            .addOnSuccessListener { result ->
                val existing = result.documents
                    .mapNotNull { it.getString("listType") }
                    .flatMap { it.split(",").map(String::trim).map(String::lowercase) }

                if (category.lowercase() in existing) {
                    Toast.makeText(context, "Категория \"$category\" уже существует", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val newDoc = mapOf(
                    "listType" to category,
                    "id" to "template_${category.lowercase()}"
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("bookLists")
                    .add(newDoc)
                    .addOnSuccessListener {
                        loadCategories(userId)
                        showDialog(false)
                        uiState = uiState.copy(newCategory = "")
                    }
            }
    }
}
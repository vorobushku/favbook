package com.example.favbook.ui.book

import android.content.Context
import android.util.Log
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

    fun dismissCategoryOptions() {
        uiState = uiState.copy(expandedCategory = null)
    }


    fun onEditCategoryChange(value: String) {
        uiState = uiState.copy(editedCategory = value)
    }

    fun updateCategory(userId: String, context: Context) {
        val oldCategory = uiState.selectedCategory ?: return
        val newCategory = uiState.editedCategory.trim()
        if (newCategory.isBlank()) return

        val userDoc = firestore.collection("users").document(userId)

        Log.d("BookViewModel", "Начинаю обновление категории: $oldCategory -> $newCategory")

        userDoc.collection("bookLists")
            .get()
            .addOnSuccessListener { result ->
                val batch = firestore.batch()
                var foundAny = false

                for (doc in result.documents) {
                    val listTypeString = doc.getString("listType") ?: continue
                    if (!listTypeString.contains(oldCategory)) continue

                    foundAny = true
                    val updatedListType = listTypeString
                        .split(",")
                        .map { it.trim() }
                        .map { if (it == oldCategory) newCategory else it }
                        .distinct()
                        .joinToString(", ")

                    val docId = doc.getString("id") ?: ""

                    if (docId.startsWith("template_")) {
                        val newId = "template_${newCategory.lowercase()}"
                        batch.update(doc.reference, mapOf(
                            "listType" to newCategory,
                            "id" to newId
                        ))
                    } else {
                        batch.update(doc.reference, "listType", updatedListType)
                    }
                }

                if (!foundAny) {
                    Log.w("BookViewModel", "Не найдено документов с категорией $oldCategory")
                }

                batch.commit().addOnSuccessListener {
                    Log.d("BookViewModel", "Категория успешно обновлена")
                    loadCategories(userId)
                    uiState = uiState.copy(
                        showEditDialog = false,
                        editedCategory = "",
                        selectedCategory = null
                    )
                }.addOnFailureListener {
                    Log.e("BookViewModel", "Ошибка при обновлении категории", it)
                    Toast.makeText(context, "Ошибка при обновлении категории", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun deleteCategory(userId: String, category: String) {
        firestore.collection("users")
            .document(userId)
            .collection("bookLists")
            .whereEqualTo("listType", category)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    doc.reference.delete()
                }
                loadCategories(userId)
                dismissCategoryOptions()
            }
    }

    fun onCategoryOptionsExpand(category: String) {
        uiState = uiState.copy(expandedCategory = category)
    }

    fun onCategoryOptionsDismiss() {
        uiState = uiState.copy(expandedCategory = null)
    }

    fun onEditCategoryDialogShow(category: String) {
        uiState = uiState.copy(
            showEditDialog = true,
            selectedCategory = category,
            editedCategory = category
        )
    }

    fun onEditCategoryDialogDismiss() {
        uiState = uiState.copy(showEditDialog = false)
    }
}
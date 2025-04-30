package com.example.favbook.ui.book

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.favbook.data.model.BookItem
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookItemViewModel @Inject constructor(
private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _lists = mutableStateOf<List<String>>(emptyList())
    val lists: State<List<String>> = _lists

    private val _userBookDocId = mutableStateOf<String?>(null)
    val userBookDocId: State<String?> = _userBookDocId

    private val _currentListType = mutableStateOf<String?>(null)
    val currentListType: State<String?> = _currentListType

    fun loadListsAndBookInfo(userId: String, bookId: String) {
        val userRef = firestore.collection("users").document(userId).collection("bookLists")

        userRef.get().addOnSuccessListener { result ->
            _lists.value = result.documents
                .mapNotNull { it.getString("listType") }
                .filter { it.lowercase() != "добавленные книги" && !it.contains(",") }
                .distinct()
        }

        userRef.whereEqualTo("id", bookId).get().addOnSuccessListener { result ->
            if (!result.isEmpty) {
                val doc = result.documents.first()
                _userBookDocId.value = doc.id
                _currentListType.value = doc.getString("listType")
            } else {
                _userBookDocId.value = null
                _currentListType.value = null
            }
        }
    }

    fun addBookToList(userId: String, book: BookItem, listType: String, onComplete: () -> Unit) {
        val bookData = book.toMap() + mapOf("listType" to listType)
        firestore.collection("users").document(userId)
            .collection("bookLists").add(bookData)
            .addOnSuccessListener { onComplete() }
    }

    fun deleteBook(userId: String, onComplete: () -> Unit) {
        _userBookDocId.value?.let { docId ->
            firestore.collection("users").document(userId)
                .collection("bookLists").document(docId)
                .delete().addOnSuccessListener { onComplete() }
        }
    }

    fun moveBook(userId: String, newList: String, onComplete: () -> Unit) {
        _userBookDocId.value?.let { docId ->
            firestore.collection("users").document(userId)
                .collection("bookLists").document(docId)
                .update("listType", newList)
                .addOnSuccessListener {
                    _currentListType.value = newList
                    onComplete()
                }
        }
    }
}
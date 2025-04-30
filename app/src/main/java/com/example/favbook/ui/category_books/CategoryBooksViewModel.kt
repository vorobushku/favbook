package com.example.favbook.ui.category_books

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.favbook.data.model.BookItem
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryBooksViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _books = mutableStateOf<List<BookItem>>(emptyList())
    val books: State<List<BookItem>> = _books

    fun loadBooksForCategory(userId: String, category: String) {
        firestore.collection("users")
            .document(userId)
            .collection("bookLists")
            .get()
            .addOnSuccessListener { result ->
                val bookList = result.documents.mapNotNull { document ->
                    val bookItem = document.toObject(BookItem::class.java)
                    val listType = document.getString("listType") ?: ""
                    val bookId = bookItem?.id ?: ""

                    val categoryList = listType.split(",").map { it.trim() }

                    if (categoryList.any { it == category } && !bookId.startsWith("template_")) {
                        bookItem
                    } else null
                }
                _books.value = bookList
            }
    }

    fun removeBookFromList(bookId: String) {
        _books.value = _books.value.filterNot { it.id == bookId }
    }
}

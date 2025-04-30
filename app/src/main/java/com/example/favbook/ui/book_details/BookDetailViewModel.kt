package com.example.favbook.ui.book_details

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.favbook.data.model.AnyBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.favbook.data.model.BookItem

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _description = mutableStateOf<String?>(null)
    val description: State<String?> = _description

    private val _authors = mutableStateOf<List<String>>(emptyList())
    val authors: State<List<String>> = _authors

    private val _lists = mutableStateOf<List<String>>(emptyList())
    val lists: State<List<String>> = _lists

    fun loadBookDetails(anyBook: AnyBook) {
        val user = auth.currentUser ?: return
        val title: String
        val initialAuthors: List<String>
        val localDescription: String?

        when (anyBook) {
            is AnyBook.GoogleBook -> {
                val book = anyBook.book
                title = book.volumeInfo.title
                initialAuthors = book.volumeInfo.authors ?: emptyList()
                localDescription = book.volumeInfo.description

                firestore.collection("users").document(user.uid).collection("bookLists")
                    .whereEqualTo("volumeInfo.title", title)
                    .get()
                    .addOnSuccessListener { result ->
                        val bookItem = result.documents.firstOrNull()?.toObject(BookItem::class.java)
                        if (bookItem?.id?.startsWith("manual") == true) {
                            _description.value = bookItem.volumeInfo.description ?: "Описание отсутствует"
                            _authors.value = bookItem.volumeInfo.authors ?: initialAuthors
                        } else {
                            _description.value = localDescription ?: "Описание не найдено"
                            _authors.value = initialAuthors
                        }
                    }
                    .addOnFailureListener {
                        _description.value = "Ошибка загрузки описания"
                    }
            }

            is AnyBook.NYTimesBook -> {
                _description.value = anyBook.book.description
                _authors.value = listOf(anyBook.book.author)
            }
        }
    }

    fun loadAvailableLists() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).collection("bookLists").get()
            .addOnSuccessListener { result ->
                val availableLists = result.documents
                    .mapNotNull { it.getString("listType") }
                    .filter { it.lowercase() != "добавленные книги" && !it.contains(",") }
                    .distinct()
                _lists.value = availableLists
            }
    }
}
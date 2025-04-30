package com.example.favbook.ui.author_books

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.remote.GoogleBooksApiService
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State
import com.example.favbook.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class AuthorBooksViewModel @Inject constructor(
    private val googleBooksApiService: GoogleBooksApiService
) : ViewModel() {
    private val _books = mutableStateOf<List<BookItem>>(emptyList())
    val books: State<List<BookItem>> = _books

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun loadBooksByAuthor(author: String) {
        viewModelScope.launch {
            try {
                val response = googleBooksApiService.searchBooks(
                    query = "inauthor:$author",
                    apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
                )
                val result = response.items.orEmpty()
                if (result.isEmpty()) {
                    _errorMessage.value = "Книги этого автора не найдены"
                } else {
                    _books.value = result
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки книг"
            }
        }
    }
}
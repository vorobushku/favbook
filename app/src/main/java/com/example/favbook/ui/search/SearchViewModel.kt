package com.example.favbook.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.model.Resource
import com.example.favbook.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel()  {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _books = MutableStateFlow<Resource<List<BookItem>>>(Resource.Success(emptyList()))
    val books: StateFlow<Resource<List<BookItem>>> = _books

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _books.value = Resource.Loading
            _books.value = repository.searchBooks(query)
        }
    }
}
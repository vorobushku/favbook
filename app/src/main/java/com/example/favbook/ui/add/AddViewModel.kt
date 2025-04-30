package com.example.favbook.ui.add

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.favbook.data.model.VolumeInfo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.favbook.data.model.BookItem
import androidx.compose.runtime.State
import com.example.favbook.data.repository.BookRepository

@HiltViewModel
class AddViewModel @Inject constructor(
    private val repository: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel()  {
    private val _uiState = mutableStateOf(AddUiState())
    val uiState: State<AddUiState> = _uiState

    private val userId get() = auth.currentUser?.uid

    init {
        fetchUserLists()
    }

    private fun fetchUserLists() {
        userId?.let { uid ->
            viewModelScope.launch {
                val lists = repository.getUserBookLists(uid)
                _uiState.value = _uiState.value.copy(lists = lists)
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onAuthorChange(author: String) {
        _uiState.value = _uiState.value.copy(author = author)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onListSelected(list: String) {
        _uiState.value = _uiState.value.copy(selectedList = list)
    }

    fun addBook(onSuccess: () -> Unit) {
        val uid = userId ?: return
        val state = _uiState.value

        val book = BookItem(
            id = "manual",
            volumeInfo = VolumeInfo(
                title = state.title,
                authors = listOf(state.author),
                description = state.description.ifEmpty { "Описание отсутствует" },
                imageLinks = null
            )
        )

        viewModelScope.launch {
            repository.addBookToUserLibrary(uid, book, state.selectedList)
            onSuccess()
        }
    }
}
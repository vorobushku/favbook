package com.example.favbook.ui.add

data class AddUiState(
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val selectedList: String? = null,
    val lists: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

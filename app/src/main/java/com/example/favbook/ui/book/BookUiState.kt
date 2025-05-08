package com.example.favbook.ui.book

data class BookUiState(
    val categories: List<String> = emptyList(),
    val newCategory: String = "",
    val showDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedCategory: String? = null,
    val expandedCategory: String? = null,
    val editedCategory: String = ""
)

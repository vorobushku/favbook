package com.example.favbook.data.model

// Модель для ответа от Google Books API
data class GoogleBooksResponse(
    val items: List<BookItem>
)

// Модель для каждого элемента книги
data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

// Модель для информации о книге
data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val description: String?
)
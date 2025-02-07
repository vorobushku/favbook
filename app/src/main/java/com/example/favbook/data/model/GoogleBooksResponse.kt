package com.example.favbook.data.model

// Модель ответа Google Books API
data class GoogleBooksResponse(
    val items: List<BookItem>?
)

// Модель данных для каждой книги
data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)
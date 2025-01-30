package com.example.favbook.data.model

//// Модель для ответа от Google Books API
//data class GoogleBooksResponse(
//    val items: List<BookItem>? // items может быть null, поэтому делаем его nullable
//)
//
//// Модель для каждого элемента книги
//data class BookItem(
//    val id: String,
//    val volumeInfo: VolumeInfo
//)
//
//// Модель для информации о книге
//data class VolumeInfo(
//    val title: String,
//    val authors: List<String>?,
//    val description: String?
//)

// Модель ответа Open Library API
data class OpenLibraryResponse(
    val docs: List<BookDoc>? // Список найденных книг
)

// Модель данных для каждой книги
data class BookDoc(
    val key: String,             // Уникальный ключ книги
    val title: String,           // Название книги
    val author_name: List<String>?, // Список авторов
    val language: List<String>?, // Новый параметр для фильтрации
    val cover_i: Int? // ID обложки книги
) {
    // Формируем URL обложки, если ID есть
    val coverUrl: String?
        get() = cover_i?.let { "https://covers.openlibrary.org/b/id/$it-S.jpg" }
}
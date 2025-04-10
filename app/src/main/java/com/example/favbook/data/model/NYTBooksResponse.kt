package com.example.favbook.data.model

data class NYTBooksResponse(
    val results: NYTListResult
)

data class NYTListResult(
    val books: List<NYTBook>
)

data class NYTBook(
    val title: String,
    val author: String,
    val book_image: String
)
package com.example.favbook.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class NYTBooksResponse(
    val results: NYTListResult
)

data class NYTListResult(
    val books: List<NYTBook>
)

@Parcelize
data class NYTBook(
    val title: String,
    val author: String,
    val book_image: String,
    val description: String
) : Parcelable
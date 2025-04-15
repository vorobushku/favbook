package com.example.favbook

import android.os.Parcelable
import com.example.favbook.data.model.BookItem
import com.example.favbook.data.model.NYTBook
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AnyBook : Parcelable {
    @Parcelize
    data class GoogleBook(val book: BookItem) : AnyBook()

    @Parcelize
    data class NYTimesBook(val book: NYTBook) : AnyBook()
}
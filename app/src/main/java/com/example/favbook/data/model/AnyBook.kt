package com.example.favbook.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class AnyBook : Parcelable {
    @Parcelize
    data class GoogleBook(val book: BookItem) : AnyBook()

    @Parcelize
    data class NYTimesBook(val book: NYTBook) : AnyBook()
}
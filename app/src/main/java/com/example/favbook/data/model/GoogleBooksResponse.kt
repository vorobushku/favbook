package com.example.favbook.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class GoogleBooksResponse(
    val items: List<BookItem>? = null
)

// Модель ответа Google Books API
@Keep
@IgnoreExtraProperties
data class BookItem(
    val id: String = "",
    val volumeInfo: VolumeInfo = VolumeInfo()
) {
    constructor() : this("", VolumeInfo()) // Пустой конструктор

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "volumeInfo" to mapOf(
                "title" to volumeInfo.title,
                "authors" to volumeInfo.authors,
                "imageLinks" to mapOf(
                    "smallThumbnail" to volumeInfo.imageLinks?.smallThumbnail,
                    "thumbnail" to volumeInfo.imageLinks?.thumbnail
                ),
                "description" to volumeInfo.description
            )
        )
    }
}

@Keep
@IgnoreExtraProperties
data class VolumeInfo(
    val title: String = "",
    val authors: List<String>? = null,
    val imageLinks: ImageLinks? = null,
    val description: String? = null
) {
    constructor() : this("", null, null, null)
}

@Keep
@IgnoreExtraProperties
data class ImageLinks(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
) {
    constructor() : this(null, null)
}
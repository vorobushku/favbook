package com.example.favbook.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Keep
@IgnoreExtraProperties
data class GoogleBooksResponse(
    val items: List<BookItem>? = null
)

@Parcelize
@Keep
@IgnoreExtraProperties
data class BookItem(
    val id: String = "",
    val volumeInfo: VolumeInfo = VolumeInfo()
) : Parcelable {
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

@Parcelize
@Keep
@IgnoreExtraProperties
data class VolumeInfo(
    val title: String = "",
    val authors: List<String>? = null,
    val imageLinks: ImageLinks? = null,
    val description: String? = null
) : Parcelable {
    constructor() : this("", null, null, null)
}

@Parcelize
@Keep
@IgnoreExtraProperties
data class ImageLinks(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
) : Parcelable {
    constructor() : this(null, null)
}
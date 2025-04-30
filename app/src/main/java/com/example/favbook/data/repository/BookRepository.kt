package com.example.favbook.data.repository

import com.example.favbook.data.model.BookItem
import com.example.favbook.data.model.NYTBook
import com.example.favbook.data.model.Resource
import com.example.favbook.data.remote.GoogleBooksApiService
import com.example.favbook.data.remote.NYTimesApiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val googleBooksApi: GoogleBooksApiService,
    private val nytBooksApi: NYTimesApiService
) {
    suspend fun searchBooks(query: String): Resource<List<BookItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = googleBooksApi.searchBooks(query)
                val books = response.items ?: emptyList()
                if (books.isNotEmpty()) {
                    Resource.Success(books)
                } else {
                    Resource.Error("Книги не найдены")
                }
            } catch (e: Exception) {
                Resource.Error("Ошибка: ${e.message}")
            }
        }
    }

    suspend fun getTopBooks(): Resource<List<NYTBook>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = nytBooksApi.getTopBooks()
                Resource.Success(response.results.books)
            } catch (e: Exception) {
                Resource.Error("Ошибка загрузки топа: ${e.message}")
            }
        }
    }

    suspend fun getUserBookLists(uid: String): List<String> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("bookLists").get().await()

        return snapshot.documents
            .mapNotNull { it.getString("listType") }
            .filter { !it.contains(",") }
            .distinct()
    }

    suspend fun addBookToUserLibrary(userId: String, book: BookItem, selectedList: String?) {
        val db = FirebaseFirestore.getInstance()
        val userBooksRef = db.collection("users").document(userId).collection("bookLists")

        val listType = listOfNotNull(selectedList, "Добавленные книги").joinToString(", ")

        val bookData = book.toMap() + mapOf(
            "listType" to listType,
            "author" to (book.volumeInfo.authors?.firstOrNull() ?: ""),
            "coverUrl" to (book.volumeInfo.imageLinks?.thumbnail ?: "")
        )

        userBooksRef.add(bookData).await()
    }
}
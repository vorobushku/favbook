package com.example.favbook.data.network

import com.example.favbook.BuildConfig
import com.example.favbook.data.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApiService {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String = BuildConfig.GOOGLE_BOOKS_API_KEY
    ): GoogleBooksResponse
}
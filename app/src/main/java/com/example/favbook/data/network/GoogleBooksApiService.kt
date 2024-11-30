package com.example.favbook.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.favbook.data.model.GoogleBooksResponse

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): GoogleBooksResponse
}
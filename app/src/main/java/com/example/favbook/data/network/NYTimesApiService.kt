package com.example.favbook.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.favbook.BuildConfig
import com.example.favbook.data.model.NYTBooksResponse

interface NYTimesApiService {
    @GET("lists/current/hardcover-fiction.json")
    suspend fun getTopBooks(
        @Query("api-key") apiKey: String = BuildConfig.NYTIMES_API_KEY
    ): NYTBooksResponse
}
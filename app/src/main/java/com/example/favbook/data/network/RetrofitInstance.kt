package com.example.favbook.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
//    private const val BASE_URL = "https://www.googleapis.com/books/v1/"
//
//    // Логгер для отслеживания запросов
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val client = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .build()
//
//    val api: GoogleBooksApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client) // добавляем client с логированием
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(GoogleBooksApiService::class.java)
//    }

    private const val BASE_URL = "https://openlibrary.org/"

    val api: GoogleBooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApiService::class.java)
    }

}
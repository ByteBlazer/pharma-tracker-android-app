package com.deltasoft.pharmatracker.api

import android.content.Context
import com.deltasoft.pharmatracker.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = BuildConfig.BASE_API_URL

    // Store the Application Context here, initialized in onCreate
    private lateinit var appContext: Context

    // Public initialization function
    @JvmStatic
    fun initialize(context: Context) {
        // Store application context to avoid memory leaks
        this.appContext = context.applicationContext
    }

    // 1. Define the AuthInterceptor using the stored context
    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(appContext)
    }

    // 2. Define the OkHttpClient with the interceptor
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }


    // 3. Define the Retrofit instance
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

//    val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

    val apiService: ApiService by lazy {
        // Ensure initialize() has been called before accessing this
        retrofit.create(ApiService::class.java)
    }
}
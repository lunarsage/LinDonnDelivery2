package com.example.lindonndelivery2.data.network

import com.example.lindonndelivery2.BuildConfig
import com.example.lindonndelivery2.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ApiClient {
    private val headerInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader(
                "Authorization",
                "Bearer ${SessionManager.accessToken ?: BuildConfig.SUPABASE_ANON_KEY}"
            )
            .addHeader("Prefer", "return=representation")
            .build()
        chain.proceed(req)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(logging)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // For PostgREST
    val rest: Retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/rest/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttp)
        .build()

    // For Supabase Auth REST
    val auth: Retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/auth/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttp)
        .build()
}

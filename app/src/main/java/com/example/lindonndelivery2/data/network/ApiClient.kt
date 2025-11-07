package com.example.lindonndelivery2.data.network

import android.util.Log
import com.example.lindonndelivery2.BuildConfig
import com.example.lindonndelivery2.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * ApiClient - Centralized HTTP client configuration for Supabase API calls
 * 
 * This object provides two Retrofit instances:
 * 1. `rest` - For PostgREST database API calls (CRUD operations)
 * 2. `auth` - For Supabase Auth API calls (authentication, user management)
 * 
 * Architecture:
 * - Uses Retrofit for type-safe HTTP client
 * - OkHttp for HTTP request/response handling
 * - Moshi for JSON serialization/deserialization
 * - Interceptors for automatic header injection and logging
 * 
 * Security:
 * - Automatically injects API key and authorization headers
 * - Uses anon key for unauthenticated requests
 * - Uses access token for authenticated requests
 * 
 * Reference: https://supabase.com/docs/reference
 */
private const val TAG = "ApiClient"

object ApiClient {
    /**
     * Header Interceptor
     * 
     * Automatically adds required headers to all HTTP requests:
     * 1. apikey: Supabase anon key (required for all requests)
     * 2. Authorization: Bearer token (access token if logged in, otherwise anon key)
     * 3. Prefer: return=representation (requests Supabase to return created/updated rows)
     * 
     * This interceptor runs before every HTTP request, ensuring all API calls
     * have the necessary authentication and configuration headers.
     */
    private val headerInterceptor = Interceptor { chain ->
        val accessToken = SessionManager.accessToken ?: BuildConfig.SUPABASE_ANON_KEY
        val authHeader = "Bearer $accessToken"
        
        Log.d(TAG, "Adding headers to request: ${chain.request().url}")
        Log.d(TAG, "Authorization header: ${if (SessionManager.accessToken != null) "Bearer [ACCESS_TOKEN]" else "Bearer [ANON_KEY]"}")
        
        val req = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Authorization", authHeader)
            .addHeader("Prefer", "return=representation")  // Return created/updated rows in response
            .build()
        chain.proceed(req)
    }

    /**
     * HTTP Logging Interceptor
     * 
     * Logs HTTP request and response details for debugging
     * Level: BASIC - logs request/response lines and headers (not body)
     * 
     * This helps with:
     * - Debugging API calls
     * - Understanding request/response flow
     * - Identifying network issues
     * 
     * Note: In production, consider disabling or using NONE level for security
     */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        // Log to Android logcat with tag "OkHttp"
    }

    /**
     * OkHttp Client
     * 
     * Configured with:
     * - Header interceptor (adds auth headers)
     * - Logging interceptor (logs requests/responses)
     * 
     * OkHttp handles:
     * - Connection pooling
     * - Request/response caching
     * - Retry logic
     * - Timeout management
     */
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)  // Add headers first
        .addInterceptor(logging)            // Then log the request
        .build()

    /**
     * Moshi JSON Converter
     * 
     * Converts JSON to/from Kotlin data classes
     * Uses KotlinJsonAdapterFactory for automatic adapter generation
     * 
     * Benefits:
     * - Type-safe JSON parsing
     * - Automatic null handling
     * - Support for Kotlin data classes
     */
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())  // Enables automatic adapter generation
        .build()

    /**
     * Retrofit Instance for PostgREST API
     * 
     * Base URL: {SUPABASE_URL}/rest/v1/
     * 
     * Used for:
     * - Database CRUD operations (restaurants, menu, orders, users)
     * - Querying data with filters, sorting, pagination
     * - Inserting, updating, deleting records
     * 
     * All database operations go through this instance
     * Reference: https://postgrest.org/
     */
    val rest: Retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/rest/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttp)
        .build()
        .also {
            Log.d(TAG, "PostgREST Retrofit instance created: ${BuildConfig.SUPABASE_URL}/rest/v1/")
        }

    /**
     * Retrofit Instance for Supabase Auth API
     * 
     * Base URL: {SUPABASE_URL}/auth/v1/
     * 
     * Used for:
     * - User authentication (signup, login, logout)
     * - Password recovery
     * - Token management
     * - User session management
     * 
     * All authentication operations go through this instance
     * Reference: https://supabase.com/docs/reference/javascript/auth-api
     */
    val auth: Retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/auth/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttp)
        .build()
        .also {
            Log.d(TAG, "Auth Retrofit instance created: ${BuildConfig.SUPABASE_URL}/auth/v1/")
        }
}

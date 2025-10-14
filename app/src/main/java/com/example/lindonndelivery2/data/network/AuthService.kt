package com.example.lindonndelivery2.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Minimal bodies for Supabase Auth REST

data class EmailPasswordBody(val email: String, val password: String)

data class SessionResponse(
    val access_token: String?,
    val token_type: String?,
    val refresh_token: String?,
    val user: Any?
)

data class UserResponse(val id: String)

interface AuthService {
    @POST("signup")
    suspend fun signUp(@Body body: EmailPasswordBody): SessionResponse

    @POST("token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: EmailPasswordBody
    ): SessionResponse

    @POST("recover")
    suspend fun recover(@Body body: Map<String, String>): Any

    @GET("user")
    suspend fun getUser(): UserResponse
}

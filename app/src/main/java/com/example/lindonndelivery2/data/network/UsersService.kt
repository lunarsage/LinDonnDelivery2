package com.example.lindonndelivery2.data.network

import com.example.lindonndelivery2.data.model.UserRow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface UsersService {
    // Upsert user by id (conflict target = id). We rely on PostgREST upsert behavior.
    @Headers(
        "Prefer: resolution=merge-duplicates"
    )
    @POST("users?on_conflict=id")
    suspend fun upsert(@Body rows: List<UserRow>): List<UserRow>

    // Fetch single user by id
    @GET("users")
    suspend fun getById(
        @Query("id") idEq: String, // e.g., eq.<uid>
        @Query("select") select: String = "*"
    ): List<UserRow>
}

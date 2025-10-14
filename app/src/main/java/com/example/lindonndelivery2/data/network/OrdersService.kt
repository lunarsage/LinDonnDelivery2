package com.example.lindonndelivery2.data.network

import com.example.lindonndelivery2.data.model.OrderCreate
import com.example.lindonndelivery2.data.model.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrdersService {
    @POST("orders")
    suspend fun create(@Body order: OrderCreate): List<OrderResponse>

    @GET("orders")
    suspend fun getById(@Query("id") idEq: String, @Query("select") select: String = "*"): List<OrderResponse>

    @GET("orders")
    suspend fun listByUid(
        @Query("uid") uidEq: String, // e.g., eq.<uid>
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): List<OrderResponse>
}

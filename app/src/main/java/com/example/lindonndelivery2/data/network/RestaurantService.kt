package com.example.lindonndelivery2.data.network

import com.example.lindonndelivery2.data.model.Restaurant
import retrofit2.http.GET

interface RestaurantService {
    @GET("restaurants?select=*")
    suspend fun getRestaurants(): List<Restaurant>
}

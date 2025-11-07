package com.example.lindonndelivery2.data.network

import com.example.lindonndelivery2.data.model.MenuItem
import retrofit2.http.GET
import retrofit2.http.Query

interface MenuService {
    @GET("menu?select=*")
    suspend fun getMenu(@Query("restaurant_id") restaurantIdEq: String): List<MenuItem>
    
    @GET("menu?select=*")
    suspend fun listByRestaurant(@Query("restaurant_id") restaurantIdEq: String): List<MenuItem>
}

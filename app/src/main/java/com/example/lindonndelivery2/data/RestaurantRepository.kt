package com.example.lindonndelivery2.data

import com.example.lindonndelivery2.data.model.Restaurant
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.RestaurantService

class RestaurantRepository {
    private val service = ApiClient.rest.create(RestaurantService::class.java)

    suspend fun getRestaurants(): List<Restaurant> = service.getRestaurants()
}

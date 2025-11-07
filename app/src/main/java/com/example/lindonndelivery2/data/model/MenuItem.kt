package com.example.lindonndelivery2.data.model

data class MenuItem(
    val id: String,
    val restaurant_id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val image_url: String? = null,
    val category: String? = null
)

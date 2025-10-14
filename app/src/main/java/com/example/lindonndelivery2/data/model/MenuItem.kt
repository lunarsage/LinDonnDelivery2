package com.example.lindonndelivery2.data.model

data class MenuItem(
    val id: String,
    val restaurant_id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image_url: String?,
    val category: String?
)

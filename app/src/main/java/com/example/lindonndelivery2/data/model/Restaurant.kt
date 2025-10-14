package com.example.lindonndelivery2.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val delivery_fee: Double,
    val avg_minutes: Int,
    val rating: Double,
    val image_url: String?
)

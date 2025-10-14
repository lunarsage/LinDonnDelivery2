package com.example.lindonndelivery2.data.model

data class OrderItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val note: String? = null
)

data class OrderCreate(
    val uid: String,
    val items: List<OrderItem>,
    val total: Double,
    val address: String,
    val status: String = "Confirmed"
)

data class OrderResponse(
    val id: String,
    val uid: String,
    val items: List<OrderItem>,
    val total: Double,
    val address: String,
    val status: String,
    val created_at: String
)

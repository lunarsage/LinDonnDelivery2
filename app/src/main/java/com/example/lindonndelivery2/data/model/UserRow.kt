package com.example.lindonndelivery2.data.model

data class UserRow(
    val id: String,
    val email: String,
    val wallet_balance: Double? = 0.0,
    val points: Int? = 0,
    val default_address: String? = null
)

package com.example.lindonndelivery2.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val delivery_fee: Double? = null,
    val avg_minutes: Int? = null,
    val rating: Double? = null,
    val image_url: String? = null
) {
    val delivery_time: String? get() = avg_minutes?.let { "${it}m" }
}

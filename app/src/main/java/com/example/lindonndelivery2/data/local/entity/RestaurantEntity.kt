package com.example.lindonndelivery2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val cuisine: String? = null,
    val image_url: String? = null,
    val delivery_time: String? = null,
    val rating: Double? = null,
    val synced: Boolean = false,
    val last_updated: Long = System.currentTimeMillis()
)


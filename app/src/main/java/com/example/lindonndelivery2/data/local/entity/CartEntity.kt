package com.example.lindonndelivery2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val item_id: String,
    val restaurant_id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val note: String? = null,
    val image_url: String? = null,
    val category: String? = null
)


package com.example.lindonndelivery2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey
    val id: String,
    val restaurant_id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val image_url: String? = null,
    val category: String? = null,
    val synced: Boolean = false,
    val last_updated: Long = System.currentTimeMillis()
)


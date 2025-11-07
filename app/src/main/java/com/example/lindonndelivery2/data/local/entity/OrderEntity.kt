package com.example.lindonndelivery2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lindonndelivery2.data.local.converter.JsonTypeConverter

@Entity(tableName = "orders")
@TypeConverters(JsonTypeConverter::class)
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val uid: String,
    val items: String, // JSON string
    val total: Double,
    val address: String,
    val status: String,
    val created_at: String,
    val synced: Boolean = false,
    val pending_sync: Boolean = false,
    val last_updated: Long = System.currentTimeMillis()
)


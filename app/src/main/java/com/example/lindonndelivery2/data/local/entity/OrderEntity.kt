package com.example.lindonndelivery2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * OrderEntity - Room entity for storing orders in local database
 * 
 * Stores order data including items as JSON string
 * Items are stored as JSON string and parsed manually when needed
 * (No TypeConverter needed since items is already a String type)
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val uid: String,
    val items: String, // JSON string - stores order items as JSON
    val total: Double,
    val address: String,
    val status: String,
    val created_at: String,
    val synced: Boolean = false,  // Whether order has been synced to server
    val pending_sync: Boolean = false,  // Whether order is pending sync
    val last_updated: Long = System.currentTimeMillis()  // Last update timestamp
)


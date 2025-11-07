package com.example.lindonndelivery2.data.local.dao

import androidx.room.*
import com.example.lindonndelivery2.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE uid = :uid ORDER BY created_at DESC")
    fun getOrdersByUser(uid: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE pending_sync = 1")
    suspend fun getPendingSyncOrders(): List<OrderEntity>

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET synced = 1, pending_sync = 0 WHERE id = :id")
    suspend fun markOrderSynced(id: String)
}


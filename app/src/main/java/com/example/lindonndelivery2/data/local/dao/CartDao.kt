package com.example.lindonndelivery2.data.local.dao

import androidx.room.*
import com.example.lindonndelivery2.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Update
    suspend fun updateCartItem(item: CartEntity)

    @Delete
    suspend fun deleteCartItem(item: CartEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM cart_items WHERE item_id = :itemId AND note = :note")
    suspend fun getCartItem(itemId: String, note: String?): CartEntity?
}


package com.example.lindonndelivery2.data.local.dao

import androidx.room.*
import com.example.lindonndelivery2.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE restaurant_id = :restaurantId ORDER BY category, name")
    fun getMenuItemsByRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: String): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMenuItems(items: List<MenuItemEntity>)

    @Query("SELECT * FROM menu_items WHERE synced = 0")
    suspend fun getUnsyncedMenuItems(): List<MenuItemEntity>

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE restaurant_id = :restaurantId")
    suspend fun clearMenuItemsForRestaurant(restaurantId: String)
}


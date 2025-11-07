package com.example.lindonndelivery2.data.local.dao

import androidx.room.*
import com.example.lindonndelivery2.data.local.entity.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY name")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantById(id: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRestaurants(restaurants: List<RestaurantEntity>)

    @Query("SELECT * FROM restaurants WHERE synced = 0")
    suspend fun getUnsyncedRestaurants(): List<RestaurantEntity>

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Query("DELETE FROM restaurants")
    suspend fun clearAll()
}


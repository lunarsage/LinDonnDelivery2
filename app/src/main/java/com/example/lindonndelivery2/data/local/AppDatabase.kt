package com.example.lindonndelivery2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lindonndelivery2.data.local.dao.*
import com.example.lindonndelivery2.data.local.entity.*

@Database(
    entities = [
        RestaurantEntity::class,
        MenuItemEntity::class,
        OrderEntity::class,
        CartEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lindonn_delivery_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


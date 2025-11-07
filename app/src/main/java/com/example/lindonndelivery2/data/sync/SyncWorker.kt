package com.example.lindonndelivery2.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val syncRepo = SyncRepository(applicationContext)
            
            // Sync restaurants
            syncRepo.syncRestaurants()
            
            // Sync pending orders
            syncRepo.syncPendingOrders()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}


package com.example.lindonndelivery2

import android.app.Application
import androidx.work.*
import com.example.lindonndelivery2.data.sync.SyncWorker
import java.util.concurrent.TimeUnit

class LinDonnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic sync
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}


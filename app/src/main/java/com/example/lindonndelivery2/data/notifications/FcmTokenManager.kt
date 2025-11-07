package com.example.lindonndelivery2.data.notifications

import android.util.Log
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.model.UserRow
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.UsersService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    
    /**
     * Gets the current FCM token and stores it in the database
     * Call this after user logs in
     */
    fun getAndStoreToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
            storeToken(token)
        }
    }
    
    /**
     * Stores FCM token in Supabase database
     */
    fun storeToken(token: String) {
        val userId = SessionManager.userId
        if (userId == null) {
            Log.d(TAG, "User not logged in, skipping FCM token storage")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersService = ApiClient.rest.create(UsersService::class.java)
                // Get current user to preserve existing data
                val currentUser = usersService.getById(idEq = "eq.$userId").firstOrNull()
                usersService.upsert(listOf(
                    UserRow(
                        id = userId,
                        email = currentUser?.email ?: "",
                        fcm_token = token,
                        default_address = currentUser?.default_address,
                        wallet_balance = currentUser?.wallet_balance,
                        points = currentUser?.points
                    )
                ))
                Log.d(TAG, "FCM token stored successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store FCM token", e)
            }
        }
    }
}


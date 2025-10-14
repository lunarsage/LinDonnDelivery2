package com.example.lindonndelivery2.data

import com.example.lindonndelivery2.BuildConfig

object SupabaseProvider {
    val baseUrl: String = BuildConfig.SUPABASE_URL
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
}

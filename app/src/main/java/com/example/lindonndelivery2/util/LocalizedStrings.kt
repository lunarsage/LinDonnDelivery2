package com.example.lindonndelivery2.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Helper object for getting localized strings in Compose
 */
object LocalizedStrings {
    @Composable
    fun getString(@androidx.annotation.StringRes resId: Int): String {
        val context = LocalContext.current
        return try {
            context.getString(resId)
        } catch (e: Exception) {
            // Fallback to resource name if string not found
            context.resources.getResourceEntryName(resId)
        }
    }
    
    @Composable
    fun getString(@androidx.annotation.StringRes resId: Int, vararg formatArgs: Any): String {
        val context = LocalContext.current
        return try {
            context.getString(resId, *formatArgs)
        } catch (e: Exception) {
            context.resources.getResourceEntryName(resId)
        }
    }
}


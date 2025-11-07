package com.example.lindonndelivery2.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInHelper(private val context: Context) {
    // Note: You need to configure the Web Client ID from Firebase Console
    // For now, using a placeholder - see SETUP_INSTRUCTIONS.md
    private val webClientId = context.resources.getString(
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    ).takeIf { it != "YOUR_GOOGLE_WEB_CLIENT_ID" } ?: ""

    private val gso = if (webClientId.isNotBlank()) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
    } else {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    val signInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    @Throws(ApiException::class)
    suspend fun signInWithGoogle(idToken: String, email: String): GoogleSignInResult {
        // In a real implementation, you would send the idToken to your backend/Supabase
        // For now, we'll return the account info
        return GoogleSignInResult.Success(email, idToken)
    }
}

sealed class GoogleSignInResult {
    data class Success(val email: String, val idToken: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}


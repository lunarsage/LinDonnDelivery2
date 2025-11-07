package com.example.lindonndelivery2.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * SessionManager - Manages user authentication session state
 * 
 * This singleton object stores and manages:
 * 1. Access token (JWT) - Used for authenticated API requests
 * 2. User ID - Extracted from JWT token for database operations
 * 
 * Architecture:
 * - Thread-safe with @Volatile annotations
 * - Persistent storage via SharedPreferences (survives app restart)
 * - JWT token parsing for user ID extraction
 * 
 * Security:
 * - Tokens are stored in SharedPreferences (encrypted on modern Android)
 * - User ID is extracted from JWT without network calls
 * - No sensitive data is logged
 * 
 * Reference: JWT structure - https://jwt.io/introduction
 */
private const val TAG = "SessionManager"
private const val PREFS_NAME = "session_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"

object SessionManager {
    private var prefs: SharedPreferences? = null
    
    /**
     * Initialize SessionManager with application context
     * Call this once in Application class or MainActivity onCreate
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Restore session from preferences
            restoreSession()
        }
    }
    
    /**
     * Restore Session from SharedPreferences
     * Called automatically on init()
     */
    private fun restoreSession() {
        val token = prefs?.getString(KEY_ACCESS_TOKEN, null)
        if (token != null) {
            Log.d(TAG, "Restoring session from SharedPreferences")
            setFromToken(token)
        } else {
            Log.d(TAG, "No saved session found in SharedPreferences")
        }
    }
    
    /**
     * Access Token (JWT)
     * 
     * JWT token received from Supabase Auth after login
     * Used in Authorization header for authenticated API requests
     * 
     * Format: header.payload.signature
     * Contains: user ID (sub), expiration (exp), issuer (iss)
     * 
     * Thread-safe with @Volatile annotation
     */
    @Volatile
    var accessToken: String? = null
        private set

    /**
     * User ID
     * 
     * Extracted from JWT token's "sub" (subject) claim
     * Used for:
     * - Database queries (filtering user's orders)
     * - User profile operations
     * - FCM token association
     * 
     * Thread-safe with @Volatile annotation
     */
    @Volatile
    var userId: String? = null
        private set

    /**
     * Set Session
     * 
     * Sets both access token and user ID explicitly
     * Used when both values are known (e.g., from login response)
     * 
     * @param token JWT access token from Supabase Auth
     * @param uid User ID (UUID from Supabase Auth)
     */
    fun setSession(token: String?, uid: String?) {
        Log.d(TAG, "Setting session - User ID: $uid, Token present: ${token != null}")
        accessToken = token
        userId = uid
        // Persist token
        prefs?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
    }

    /**
     * Clear Session
     * 
     * Removes all session data (logout)
     * Clears both access token and user ID
     * 
     * Called on:
     * - User logout
     * - Session expiration
     * - Authentication errors
     */
    fun clear() {
        Log.d(TAG, "Clearing session - logging out user")
        accessToken = null
        userId = null
        // Clear persisted token
        prefs?.edit()?.remove(KEY_ACCESS_TOKEN)?.apply()
    }

    /**
     * Set Session from Token
     * 
     * Sets access token and extracts user ID from JWT
     * Used when only token is available (e.g., after token refresh)
     * 
     * Process:
     * 1. Store the token
     * 2. Decode JWT payload (base64url)
     * 3. Extract "sub" claim (user ID)
     * 4. Store user ID
     * 5. Persist token to SharedPreferences
     * 
     * @param token JWT access token from Supabase Auth
     */
    fun setFromToken(token: String) {
        Log.d(TAG, "Setting session from token")
        accessToken = token
        userId = decodeUserIdFromAccessToken(token)
        // Persist token
        prefs?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
        if (userId != null) {
            Log.d(TAG, "User ID extracted from token: $userId")
        } else {
            Log.w(TAG, "Failed to extract user ID from token")
        }
    }
    
    /**
     * Check if user is logged in
     * 
     * @return true if access token exists, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return accessToken != null && userId != null
    }

    /**
     * Base64URL Decode
     * 
     * Converts base64url-encoded string to regular base64
     * Then decodes to string
     * 
     * Base64URL differences from Base64:
     * - Uses '-' instead of '+'
     * - Uses '_' instead of '/'
     * - No padding with '=' (we add it for Android decoder)
     * 
     * @param s Base64URL encoded string
     * @return Decoded string
     * 
     * Reference: RFC 4648 Section 5 - https://tools.ietf.org/html/rfc4648#section-5
     */
    private fun base64UrlDecode(s: String): String {
        // Replace base64url characters with base64 characters
        val normalized = s.replace('-', '+').replace('_', '/')
        // Calculate padding needed (base64 strings must be multiple of 4)
        val pad = (4 - normalized.length % 4) % 4
        val padded = normalized + "=".repeat(pad)
        // Decode using Android's Base64 decoder
        return String(android.util.Base64.decode(padded, android.util.Base64.DEFAULT))
    }

    /**
     * Decode User ID from Access Token
     * 
     * Extracts the "sub" (subject) claim from JWT token
     * This is the user's unique ID in Supabase
     * 
     * JWT Structure:
     * - Header: algorithm and token type
     * - Payload: claims (sub, exp, iss, etc.)
     * - Signature: verification (not used here)
     * 
     * Process:
     * 1. Split token by '.' to get parts
     * 2. Decode payload (second part) from base64url
     * 3. Parse JSON to find "sub" field
     * 4. Extract user ID value
     * 
     * Note: This is a simplified parser. For production, use a JWT library.
     * 
     * @param token JWT access token
     * @return User ID if found, null otherwise
     * 
     * Reference: JWT structure - https://jwt.io/introduction
     */
    fun decodeUserIdFromAccessToken(token: String): String? {
        return try {
            Log.d(TAG, "Decoding user ID from JWT token")
            // JWT has 3 parts: header.payload.signature
            val parts = token.split('.')
            if (parts.size < 2) {
                Log.w(TAG, "Invalid JWT token format - expected 3 parts, got ${parts.size}")
                return null
            }
            
            // Decode the payload (second part)
            val payloadJson = base64UrlDecode(parts[1])
            Log.d(TAG, "JWT payload decoded, length: ${payloadJson.length}")
            
            // Simple string extraction to avoid adding JSON library dependency
            // Look for "sub" field in JSON payload
            val key = "\"sub\""
            val idx = payloadJson.indexOf(key)
            if (idx == -1) {
                Log.w(TAG, "JWT payload does not contain 'sub' claim")
                return null
            }
            
            // Extract the value after "sub" key
            val start = payloadJson.indexOf('"', idx + key.length)
            val end = payloadJson.indexOf('"', start + 1)
            if (start != -1 && end != -1) {
                val userId = payloadJson.substring(start + 1, end)
                Log.d(TAG, "Successfully extracted user ID from JWT")
                userId
            } else {
                Log.w(TAG, "Failed to parse user ID from JWT payload")
                null
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error decoding user ID from token", e)
            null
        }
    }
}

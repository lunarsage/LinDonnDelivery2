package com.example.lindonndelivery2.data

object SessionManager {
    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var userId: String? = null
        private set

    fun setSession(token: String?, uid: String?) {
        accessToken = token
        userId = uid
    }

    fun clear() {
        accessToken = null
        userId = null
    }

    fun setFromToken(token: String) {
        accessToken = token
        userId = decodeUserIdFromAccessToken(token)
    }

    private fun base64UrlDecode(s: String): String {
        val normalized = s.replace('-', '+').replace('_', '/')
        val pad = (4 - normalized.length % 4) % 4
        val padded = normalized + "=".repeat(pad)
        return String(android.util.Base64.decode(padded, android.util.Base64.DEFAULT))
    }

    // Extracts `sub` claim from JWT without verification
    fun decodeUserIdFromAccessToken(token: String): String? {
        return try {
            val parts = token.split('.')
            if (parts.size < 2) return null
            val payloadJson = base64UrlDecode(parts[1])
            // naive extraction to avoid adding a JSON lib here
            val key = "\"sub\""
            val idx = payloadJson.indexOf(key)
            if (idx == -1) return null
            val start = payloadJson.indexOf('"', idx + key.length)
            val end = payloadJson.indexOf('"', start + 1)
            if (start != -1 && end != -1) payloadJson.substring(start + 1, end) else null
        } catch (_: Throwable) {
            null
        }
    }
}

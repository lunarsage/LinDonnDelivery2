package com.example.lindonndelivery2.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.auth.GoogleSignInHelper
import com.example.lindonndelivery2.data.model.UserRow
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.AuthService
import com.example.lindonndelivery2.data.network.EmailPasswordBody
import com.example.lindonndelivery2.data.network.UsersService
import com.example.lindonndelivery2.data.notifications.FcmTokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LinDonn Delivery 2", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = RustOrange,
                focusedIndicatorColor = RustOrange,
                unfocusedIndicatorColor = Grey300,
                focusedLabelColor = Grey700,
                unfocusedLabelColor = Grey700
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = RustOrange,
                focusedIndicatorColor = RustOrange,
                unfocusedIndicatorColor = Grey300,
                focusedLabelColor = Grey700,
                unfocusedLabelColor = Grey700
            )
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        val auth = remember { ApiClient.auth.create(AuthService::class.java) }
        val users = remember { ApiClient.rest.create(UsersService::class.java) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        loading = true; error = null
                        try {
                            val res = auth.signIn(body = EmailPasswordBody(email, password))
                            if (res.access_token.isNullOrBlank()) throw IllegalStateException("Login failed")
                            SessionManager.setFromToken(res.access_token)
                            // Ensure user exists in public.users for FK on orders
                            SessionManager.userId?.let { uid ->
                                try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                            }
                            // Get and store FCM token after login
                            FcmTokenManager.getAndStoreToken()
                            onSuccess()
                        } catch (t: Throwable) {
                            error = t.message
                        } finally { loading = false }
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text(if (loading) "Signing in..." else "Sign In") }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        loading = true; error = null
                        try {
                            // Validate email and password
                            if (email.isBlank()) {
                                error = "Please enter your email"
                                loading = false
                                return@launch
                            }
                            if (password.isBlank() || password.length < 6) {
                                error = "Password must be at least 6 characters"
                                loading = false
                                return@launch
                            }
                            
                            // Try login first (in case account already exists), avoids extra requests
                            try {
                                val login1 = auth.signIn(body = EmailPasswordBody(email, password))
                                if (!login1.access_token.isNullOrBlank()) {
                                    SessionManager.setFromToken(login1.access_token!!)
                                    // Ensure user exists in public.users for FK on orders
                                    SessionManager.userId?.let { uid ->
                                        try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                    }
                                    // Get and store FCM token after login
                                    FcmTokenManager.getAndStoreToken()
                                    onSuccess()
                                    return@launch
                                }
                            } catch (e: Throwable) { 
                                // If login fails with 400, it might mean account doesn't exist, proceed to signup
                                if (e.message?.contains("400") == false && e.message?.contains("Invalid") == false) {
                                    throw e
                                }
                                // Otherwise proceed to sign up
                            }

                            // Create account
                            val signUpResponse = auth.signUp(EmailPasswordBody(email, password))
                            
                            // Check if signup was successful
                            if (signUpResponse.access_token != null && signUpResponse.access_token.isNotBlank()) {
                                // Signup returned token directly (email confirmation disabled)
                                SessionManager.setFromToken(signUpResponse.access_token)
                                SessionManager.userId?.let { uid ->
                                    try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                }
                                FcmTokenManager.getAndStoreToken()
                                onSuccess()
                            } else {
                                // Signup successful but email confirmation required, or need to login
                                error = "Account created! Please check your email to confirm, then sign in."
                                // Try to login after a delay (in case confirmation is not required)
                                delay(1000)
                                try {
                                    val login2 = auth.signIn(body = EmailPasswordBody(email, password))
                                    if (!login2.access_token.isNullOrBlank()) {
                                        SessionManager.setFromToken(login2.access_token!!)
                                        SessionManager.userId?.let { uid ->
                                            try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                        }
                                        FcmTokenManager.getAndStoreToken()
                                        onSuccess()
                                        return@launch
                                    }
                                } catch (_: Throwable) {
                                    // Login failed, email confirmation likely required
                                }
                            }
                        } catch (t: Throwable) {
                            val msg = t.message ?: "Sign up failed"
                            error = when {
                                msg.contains("429") || msg.contains("Too Many", ignoreCase = true) -> 
                                    "Too many attempts. Please wait a bit and try again."
                                msg.contains("400") -> 
                                    "Invalid email or password. Password must be at least 6 characters."
                                msg.contains("User already registered", ignoreCase = true) ->
                                    "Email already registered. Please sign in instead."
                                else -> msg
                            }
                        } finally { loading = false }
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text("Sign Up") }
        }

        TextButton(onClick = {
            scope.launch {
                if (email.isNotBlank()) {
                    try {
                        auth.recover(mapOf("email" to email))
                        error = "Password reset email sent"
                    } catch (t: Throwable) {
                        error = t.message
                    }
                } else error = "Enter email first"
            }
        }) { Text("Forgot password?") }
        
        Spacer(Modifier.height(16.dp))
        
        // Google Sign-In / Sign-Up
        val context = LocalContext.current
        val googleSignInHelper = remember { GoogleSignInHelper(context) }
        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.let { acct ->
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                val email = acct.email ?: throw IllegalStateException("No email from Google account")
                                android.util.Log.d("LoginScreen", "Google Sign-In successful for email: $email")
                                
                                // For Google Sign-In, we use a deterministic password based on Google ID
                                // This ensures the same password is used each time for the same Google account
                                // The password is derived from the Google account ID, making it consistent
                                val googleId = acct.id ?: email
                                val tempPassword = "GoogleAuth_${googleId.hashCode().toString().replace("-", "N")}_${email.hashCode()}"
                                
                                // Try to sign in first (user might already exist)
                                android.util.Log.d("LoginScreen", "Attempting to sign in existing user...")
                                try {
                                    val loginRes = auth.signIn(body = EmailPasswordBody(email, tempPassword))
                                    
                                    if (!loginRes.access_token.isNullOrBlank()) {
                                        android.util.Log.d("LoginScreen", "Sign in successful for existing user")
                                        SessionManager.setFromToken(loginRes.access_token!!)
                                        SessionManager.userId?.let { uid ->
                                            try { 
                                                users.upsert(listOf(UserRow(id = uid, email = email))) 
                                                android.util.Log.d("LoginScreen", "User record updated in database")
                                            } catch (e: Throwable) {
                                                android.util.Log.e("LoginScreen", "Failed to update user record", e)
                                            }
                                        }
                                        FcmTokenManager.getAndStoreToken()
                                        loading = false
                                        onSuccess()
                                        return@launch
                                    }
                                } catch (loginError: Throwable) {
                                    android.util.Log.d("LoginScreen", "Login failed (user may not exist): ${loginError.message}")
                                    // User doesn't exist, try to create account
                                }
                                
                                // User doesn't exist, create account
                                android.util.Log.d("LoginScreen", "Creating new account for Google user...")
                                try {
                                    val signUpRes = auth.signUp(EmailPasswordBody(email, tempPassword))
                                    android.util.Log.d("LoginScreen", "Sign up response received")
                                    
                                    if (signUpRes.access_token != null && signUpRes.access_token.isNotBlank()) {
                                        // Account created and signed in immediately
                                        android.util.Log.d("LoginScreen", "Account created and signed in")
                                        SessionManager.setFromToken(signUpRes.access_token)
                                        SessionManager.userId?.let { uid ->
                                            try { 
                                                users.upsert(listOf(UserRow(id = uid, email = email))) 
                                                android.util.Log.d("LoginScreen", "User record created in database")
                                            } catch (e: Throwable) {
                                                android.util.Log.e("LoginScreen", "Failed to create user record", e)
                                            }
                                        }
                                        FcmTokenManager.getAndStoreToken()
                                        loading = false
                                        onSuccess()
                                        return@launch
                                    } else {
                                        // Signup successful but email confirmation might be required
                                        // Try to sign in after a short delay
                                        android.util.Log.d("LoginScreen", "Signup completed, attempting to sign in...")
                                        delay(1000)
                                        try {
                                            val loginRes = auth.signIn(body = EmailPasswordBody(email, tempPassword))
                                            if (!loginRes.access_token.isNullOrBlank()) {
                                                android.util.Log.d("LoginScreen", "Sign in successful after signup")
                                                SessionManager.setFromToken(loginRes.access_token!!)
                                                SessionManager.userId?.let { uid ->
                                                    try { 
                                                        users.upsert(listOf(UserRow(id = uid, email = email))) 
                                                    } catch (_: Throwable) {}
                                                }
                                                FcmTokenManager.getAndStoreToken()
                                                loading = false
                                                onSuccess()
                                                return@launch
                                            }
                                        } catch (loginAfterSignupError: Throwable) {
                                            android.util.Log.e("LoginScreen", "Failed to sign in after signup", loginAfterSignupError)
                                            error = "Account created! Please check your email to confirm, then sign in with email/password."
                                            loading = false
                                            return@launch
                                        }
                                    }
                                } catch (signUpError: Throwable) {
                                    android.util.Log.e("LoginScreen", "Sign up failed", signUpError)
                                    val errorMsg = signUpError.message ?: "Unknown error"
                                    error = when {
                                        errorMsg.contains("already registered", ignoreCase = true) -> {
                                            // User exists but password doesn't match - try to sign in with a different approach
                                            android.util.Log.d("LoginScreen", "User already registered, attempting alternative sign in...")
                                            "Google account already registered. Please sign in with email/password or use a different Google account."
                                        }
                                        errorMsg.contains("400") -> {
                                            "Invalid email or password format. Please try email/password signup."
                                        }
                                        else -> "Failed to create account: $errorMsg. Please try email/password signup."
                                    }
                                }
                            } catch (t: Throwable) {
                                android.util.Log.e("LoginScreen", "Google Sign-In error", t)
                                error = "Google Sign-In error: ${t.message ?: "Unknown error"}"
                            } finally {
                                loading = false
                            }
                        }
                    } ?: run {
                        error = "Failed to get Google account information"
                    }
                } catch (e: ApiException) {
                    android.util.Log.e("LoginScreen", "Google Sign-In API exception", e)
                    loading = false
                    error = when (e.statusCode) {
                        12501 -> "Google Sign-In was cancelled"
                        10 -> "Google Sign-In failed: Developer error. Please check Google configuration."
                        7 -> "Google Sign-In failed: Network error. Please check your connection."
                        else -> "Google Sign-In failed: ${e.message} (Code: ${e.statusCode})"
                    }
                }
            } else {
                loading = false
                android.util.Log.d("LoginScreen", "Google Sign-In result not OK: ${result.resultCode}")
                // Don't show error for user cancellation (resultCode != RESULT_OK is normal if user cancels)
            }
        }
        
        OutlinedButton(
            onClick = {
                android.util.Log.d("LoginScreen", "Google Sign-In button clicked")
                try {
                    val signInIntent = googleSignInHelper.signInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    android.util.Log.e("LoginScreen", "Failed to launch Google Sign-In", e)
                    error = "Failed to start Google Sign-In: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(context.getString(com.example.lindonndelivery2.R.string.sign_in_with_google))
        }
    }
}

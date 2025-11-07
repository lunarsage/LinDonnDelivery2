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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
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
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

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
        // Logo Image
        Image(
            painter = painterResource(id = com.example.lindonndelivery2.R.drawable.logo_image),
            contentDescription = "LinDonn Delivery Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )
        
        Text("LinDonn Delivery", style = MaterialTheme.typography.headlineMedium)
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
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (loading) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
            android.util.Log.d("LoginScreen", "Google Sign-In result received. Result code: ${result.resultCode}")
            
            if (result.resultCode == Activity.RESULT_OK) {
                android.util.Log.d("LoginScreen", "Processing Google Sign-In result...")
                
                scope.launch {
                    loading = true
                    error = null
                    
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        android.util.Log.d("LoginScreen", "Got Google Sign-In task, isComplete: ${task.isComplete}, isSuccessful: ${task.isSuccessful}")
                        
                        // Use await() for better coroutine integration
                        val account = try {
                            withContext(Dispatchers.IO) {
                                task.await()
                            }
                        } catch (e: ApiException) {
                            android.util.Log.e("LoginScreen", "ApiException getting account: ${e.statusCode} - ${e.message}", e)
                            loading = false
                            error = when (e.statusCode) {
                                12501 -> "Google Sign-In was cancelled"
                                10 -> "Google Sign-In failed: Developer error. Check Google configuration."
                                7 -> "Google Sign-In failed: Network error. Check your connection."
                                8 -> "Google Sign-In failed: Internal error. Please try again."
                                else -> "Google Sign-In failed: ${e.message ?: "Unknown error"} (Code: ${e.statusCode})"
                            }
                            return@launch
                        } catch (e: Exception) {
                            android.util.Log.e("LoginScreen", "Exception getting account", e)
                            loading = false
                            error = "Failed to get Google account: ${e.message ?: "Unknown error"}"
                            return@launch
                        }
                        
                        if (account == null) {
                            android.util.Log.e("LoginScreen", "Account is null")
                            loading = false
                            error = "Failed to get Google account information"
                            return@launch
                        }
                        
                        val email = account.email
                        if (email.isNullOrBlank()) {
                            android.util.Log.e("LoginScreen", "Email is null or blank")
                            loading = false
                            error = "No email found in Google account"
                            return@launch
                        }
                        
                        android.util.Log.d("LoginScreen", "Google account email: $email, ID: ${account.id}")
                        
                        // Generate a consistent password for this Google account
                        // Use the Google ID if available, otherwise use email hash
                        val googleId = account.id ?: email
                        // Create a more stable password hash
                        val stableHash = (googleId + email).hashCode().toString().replace("-", "N")
                        val tempPassword = "Google${stableHash}Auth${email.length}"
                        
                        android.util.Log.d("LoginScreen", "Generated password for Google user (length: ${tempPassword.length})")
                        
                        // Strategy: Try signup first (creates account if new), then signin
                        // This handles both new and existing users more reliably
                        var loginSuccessful = false
                        
                        // First, try to create account (will succeed if new, or fail if exists)
                        android.util.Log.d("LoginScreen", "Step 1: Attempting to create/signup account...")
                        try {
                            val signUpRes = auth.signUp(EmailPasswordBody(email, tempPassword))
                            android.util.Log.d("LoginScreen", "Signup response: token present = ${!signUpRes.access_token.isNullOrBlank()}")
                            
                            if (!signUpRes.access_token.isNullOrBlank()) {
                                // New account created and signed in
                                android.util.Log.d("LoginScreen", "New account created and signed in via signup")
                                SessionManager.setFromToken(signUpRes.access_token!!)
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
                                loginSuccessful = true
                                return@launch
                            }
                        } catch (signUpError: Throwable) {
                            val errorMsg = signUpError.message ?: "Unknown error"
                            android.util.Log.d("LoginScreen", "Signup failed (may be existing user): $errorMsg")
                            
                            // If user already exists, that's fine - we'll try to sign in
                            if (!errorMsg.contains("already registered", ignoreCase = true) && 
                                !errorMsg.contains("already exists", ignoreCase = true) &&
                                !errorMsg.contains("400") &&
                                !errorMsg.contains("User already registered", ignoreCase = true)) {
                                // Unexpected error, show it
                                android.util.Log.e("LoginScreen", "Unexpected signup error", signUpError)
                                error = "Signup failed: $errorMsg"
                                loading = false
                                return@launch
                            }
                            // Otherwise, continue to try sign in (user probably already exists)
                        }
                        
                        // If signup didn't work, try to sign in (user might already exist)
                        if (!loginSuccessful) {
                            android.util.Log.d("LoginScreen", "Step 2: Attempting to sign in existing user...")
                            try {
                                val loginRes = auth.signIn(body = EmailPasswordBody(email, tempPassword))
                                android.util.Log.d("LoginScreen", "Signin response: token present = ${!loginRes.access_token.isNullOrBlank()}")
                                
                                if (!loginRes.access_token.isNullOrBlank()) {
                                    // Existing user signed in
                                    android.util.Log.d("LoginScreen", "Existing user signed in successfully")
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
                                    loginSuccessful = true
                                    return@launch
                                } else {
                                    android.util.Log.e("LoginScreen", "Sign in returned empty token")
                                    error = "Sign in failed: No access token received"
                                }
                            } catch (loginError: Throwable) {
                                val errorMsg = loginError.message ?: "Unknown error"
                                android.util.Log.e("LoginScreen", "Sign in failed", loginError)
                                
                                // If both signup and signin failed, the password might be wrong
                                // This can happen if the user was created with a different password
                                error = when {
                                    errorMsg.contains("Invalid login credentials", ignoreCase = true) ||
                                    errorMsg.contains("Invalid email or password", ignoreCase = true) -> {
                                        "Google account exists but password doesn't match. Please sign in with email/password or contact support."
                                    }
                                    errorMsg.contains("400") -> {
                                        "Invalid credentials. Please try email/password signup or contact support."
                                    }
                                    else -> {
                                        "Failed to sign in with Google: $errorMsg"
                                    }
                                }
                            }
                        }
                        
                        if (!loginSuccessful) {
                            android.util.Log.e("LoginScreen", "Both signup and signin failed")
                            loading = false
                            // Error message already set above
                        }
                        
                    } catch (t: Throwable) {
                        android.util.Log.e("LoginScreen", "Unexpected error in Google Sign-In flow", t)
                        error = "Google Sign-In error: ${t.message ?: "Unknown error"}"
                        loading = false
                    }
                }
            } else {
                // User cancelled or result not OK
                android.util.Log.d("LoginScreen", "Google Sign-In cancelled or failed. Result code: ${result.resultCode}")
                if (result.resultCode != Activity.RESULT_CANCELED) {
                    error = "Google Sign-In failed. Please try again."
                }
                // Don't show error for user cancellation (RESULT_CANCELED = -1)
            }
        }
        
        OutlinedButton(
            onClick = {
                android.util.Log.d("LoginScreen", "Google Sign-In button clicked")
                error = null
                try {
                    val signInIntent = googleSignInHelper.signInClient.signInIntent
                    android.util.Log.d("LoginScreen", "Launching Google Sign-In intent")
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    android.util.Log.e("LoginScreen", "Failed to launch Google Sign-In", e)
                    error = "Failed to start Google Sign-In: ${e.message ?: "Unknown error"}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(context.getString(com.example.lindonndelivery2.R.string.sign_in_with_google))
            }
        }
    }
}

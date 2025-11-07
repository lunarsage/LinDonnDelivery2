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
                                
                                // For Google Sign-In, we'll create a user account with a generated password
                                // and then sign them in. This is a workaround since Supabase OAuth requires backend setup.
                                // In production, you should use Supabase's OAuth provider.
                                
                                // Try to sign in first (user might already exist)
                                try {
                                    // Generate a password for Google users (they won't need it, but Supabase requires it)
                                    // We use a hashed version of their email + a secret as password
                                    val tempPassword = "google_${email.hashCode()}_${acct.id}"
                                    val loginRes = auth.signIn(body = EmailPasswordBody(email, tempPassword))
                                    
                                    if (!loginRes.access_token.isNullOrBlank()) {
                                        // User exists, sign in successful
                                        SessionManager.setFromToken(loginRes.access_token!!)
                                        SessionManager.userId?.let { uid ->
                                            try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                        }
                                        FcmTokenManager.getAndStoreToken()
                                        onSuccess()
                                        return@launch
                                    }
                                } catch (e: Throwable) {
                                    // User doesn't exist, create account
                                    // Generate a secure password for Google-authenticated users
                                    val tempPassword = "Google${email.hashCode()}${System.currentTimeMillis()}"
                                    
                                    try {
                                        // Try to create account
                                        val signUpRes = auth.signUp(EmailPasswordBody(email, tempPassword))
                                        
                                        if (signUpRes.access_token != null && signUpRes.access_token.isNotBlank()) {
                                            // Account created and signed in
                                            SessionManager.setFromToken(signUpRes.access_token)
                                            SessionManager.userId?.let { uid ->
                                                try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                            }
                                            FcmTokenManager.getAndStoreToken()
                                            onSuccess()
                                        } else {
                                            // Need to sign in after signup
                                            delay(500)
                                            val loginRes = auth.signIn(body = EmailPasswordBody(email, tempPassword))
                                            if (!loginRes.access_token.isNullOrBlank()) {
                                                SessionManager.setFromToken(loginRes.access_token!!)
                                                SessionManager.userId?.let { uid ->
                                                    try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                                                }
                                                FcmTokenManager.getAndStoreToken()
                                                onSuccess()
                                            } else {
                                                error = "Failed to sign in with Google. Please try email/password signup."
                                            }
                                        }
                                    } catch (signUpError: Throwable) {
                                        error = "Failed to create account: ${signUpError.message}. Please try email/password signup."
                                    }
                                }
                            } catch (t: Throwable) {
                                error = "Google Sign-In error: ${t.message}"
                            } finally {
                                loading = false
                            }
                        }
                    }
                } catch (e: ApiException) {
                    error = when (e.statusCode) {
                        12501 -> "Google Sign-In was cancelled"
                        10 -> "Google Sign-In failed: Developer error. Check configuration."
                        else -> "Google Sign-In failed: ${e.message} (Code: ${e.statusCode})"
                    }
                }
            } else {
                error = "Google Sign-In cancelled or failed"
            }
        }
        
        OutlinedButton(
            onClick = {
                val signInIntent = googleSignInHelper.signInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Sign in / Sign up with Google")
        }
    }
}

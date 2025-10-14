package com.example.lindonndelivery2.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.AuthService
import com.example.lindonndelivery2.data.network.EmailPasswordBody
import com.example.lindonndelivery2.data.network.UsersService
import com.example.lindonndelivery2.data.model.UserRow
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
                            // Try login first (in case account already exists), avoids extra requests
                            try {
                                val login1 = auth.signIn(body = EmailPasswordBody(email, password))
                                if (!login1.access_token.isNullOrBlank()) {
                                    SessionManager.setFromToken(login1.access_token!!)
                                    onSuccess()
                                    return@launch
                                }
                            } catch (_: Throwable) { /* proceed to sign up */ }

                            // Create account
                            auth.signUp(EmailPasswordBody(email, password))

                            // Small delay to avoid immediate rate limit or propagation issues
                            delay(600)

                            // Login after signup
                            val login2 = auth.signIn(body = EmailPasswordBody(email, password))
                            if (login2.access_token.isNullOrBlank()) throw IllegalStateException("Sign up/login failed")
                            SessionManager.setFromToken(login2.access_token!!)
                            // Ensure user exists in public.users for FK on orders
                            SessionManager.userId?.let { uid ->
                                try { users.upsert(listOf(UserRow(id = uid, email = email))) } catch (_: Throwable) {}
                            }
                            onSuccess()
                        } catch (t: Throwable) {
                            val msg = t.message ?: ""
                            error = if (msg.contains("429") || msg.contains("Too Many" , ignoreCase = true))
                                "Too many attempts. Please wait a bit and try again." else msg
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
    }
}

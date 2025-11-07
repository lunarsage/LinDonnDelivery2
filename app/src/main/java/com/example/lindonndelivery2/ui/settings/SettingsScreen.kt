package com.example.lindonndelivery2.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.MainActivity
import com.example.lindonndelivery2.util.LocaleHelper

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        // Language Selection
        Text(
            text = "Language / Taal / Ulimi",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedLanguage == "en",
                onClick = {
                    selectedLanguage = "en"
                    LocaleHelper.saveLanguage(context, "en")
                },
                label = { Text("English") }
            )
            FilterChip(
                selected = selectedLanguage == "af",
                onClick = {
                    selectedLanguage = "af"
                    LocaleHelper.saveLanguage(context, "af")
                },
                label = { Text("Afrikaans") }
            )
            FilterChip(
                selected = selectedLanguage == "zu",
                onClick = {
                    selectedLanguage = "zu"
                    LocaleHelper.saveLanguage(context, "zu")
                },
                label = { Text("isiZulu") }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Push Notifications",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Marketing Emails",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = marketingEnabled,
                onCheckedChange = { marketingEnabled = it }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // Restart activity to apply language change
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Settings")
        }
    }
}


package com.example.lindonndelivery2.ui.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.MainActivity
import com.example.lindonndelivery2.util.LocaleHelper
import java.util.Locale

private const val TAG = "SettingsScreen"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var selectedLanguage by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }
    
    // Update selectedLanguage when locale changes externally
    LaunchedEffect(configuration.locales[0].language) {
        selectedLanguage = configuration.locales[0].language
    }

    // Get localized strings
    val resources = context.resources
    val settingsText = try {
        resources.getString(resources.getIdentifier("settings", "string", context.packageName))
    } catch (e: Exception) { "Settings" }
    
    val languageText = try {
        resources.getString(resources.getIdentifier("language", "string", context.packageName))
    } catch (e: Exception) { "Language" }
    
    val notificationsText = try {
        resources.getString(resources.getIdentifier("notifications", "string", context.packageName))
    } catch (e: Exception) { "Notifications" }
    
    val saveText = try {
        resources.getString(resources.getIdentifier("save", "string", context.packageName))
    } catch (e: Exception) { "Save" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = settingsText,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        // Language Selection
        Text(
            text = "$languageText / Taal / Ulimi",
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
                    Log.d(TAG, "Language changed to: English")
                    selectedLanguage = "en"
                    LocaleHelper.saveLanguage(context, "en")
                },
                label = { Text("English") }
            )
            FilterChip(
                selected = selectedLanguage == "af",
                onClick = {
                    Log.d(TAG, "Language changed to: Afrikaans")
                    selectedLanguage = "af"
                    LocaleHelper.saveLanguage(context, "af")
                },
                label = { Text("Afrikaans") }
            )
            FilterChip(
                selected = selectedLanguage == "zu",
                onClick = {
                    Log.d(TAG, "Language changed to: isiZulu")
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
                text = notificationsText,
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
                Log.d(TAG, "Save settings clicked - Language: $selectedLanguage")
                // Language is already saved when user clicks the filter chip
                // Restart activity to apply language change to entire app
                // SessionManager will restore auth state automatically
                Log.d(TAG, "Restarting activity to apply language change (auth state will be preserved)")
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                (context as? android.app.Activity)?.finish()
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(saveText)
        }
    }
}


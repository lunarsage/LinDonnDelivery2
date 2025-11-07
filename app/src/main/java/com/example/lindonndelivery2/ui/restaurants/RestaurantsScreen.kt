package com.example.lindonndelivery2.ui.restaurants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.RestaurantRepository
import com.example.lindonndelivery2.data.model.Restaurant
import kotlinx.coroutines.launch
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange

@Composable
fun RestaurantsScreen(onRestaurantClick: (Restaurant) -> Unit) {
    val context = LocalContext.current
    val repo = remember { RestaurantRepository() }
    var items by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                items = repo.getRestaurants()
            } catch (t: Throwable) {
                error = t.message
            } finally { loading = false }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(context.getString(com.example.lindonndelivery2.R.string.restaurants), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(context.getString(com.example.lindonndelivery2.R.string.search_restaurants)) },
            singleLine = true,
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
            ),
        )
        Spacer(Modifier.height(12.dp))
        when {
            loading -> Text(context.getString(com.example.lindonndelivery2.R.string.loading), style = MaterialTheme.typography.bodyMedium)
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            else -> {
                val filtered = if (query.isBlank()) items else items.filter { r ->
                    r.name.contains(query, ignoreCase = true) || r.cuisine.contains(query, ignoreCase = true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onRestaurantClick(r) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(r.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text("${r.cuisine} • ${r.avg_minutes?.let { "${it}m" } ?: "N/A"} • R${r.delivery_fee ?: 0.0} • ★${r.rating ?: 0.0}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.lindonndelivery2.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.cart.CartStore
import com.example.lindonndelivery2.data.model.MenuItem
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.MenuService
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.clickable

@Composable
fun MenuScreen(
    restaurantId: String,
    restaurantName: String,
    onBack: () -> Unit,
    onViewCart: () -> Unit
) {
    val service = remember { ApiClient.rest.create(MenuService::class.java) }
    var items by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val sections = listOf("All", "Mains", "Sides", "Drinks")
    val expanded = remember { mutableStateMapOf<String, Boolean>().apply { sections.forEach { this[it] = it == "All" } } }
    val scope = rememberCoroutineScope()

    LaunchedEffect(restaurantId) {
        scope.launch {
            try {
                items = service.getMenu(restaurantIdEq = "eq.$restaurantId")
            } catch (t: Throwable) {
                error = t.message
            } finally { loading = false }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(restaurantName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        when {
            loading -> Text("Loading menu...", style = MaterialTheme.typography.bodyMedium)
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            else -> {
                val grouped = items.groupBy { it.category?.ifBlank { "Uncategorised" } ?: "Uncategorised" }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sections.forEach { section ->
                        val list = if (section == "All") items else grouped[section] ?: emptyList()
                        if (list.isNotEmpty()) {
                            item(key = "header_$section") {
                                CategoryHeader(
                                    title = section,
                                    expanded = expanded[section] == true,
                                    onToggle = { expanded[section] = !(expanded[section] ?: false) }
                                )
                            }
                            if (expanded[section] == true) {
                                items(list, key = { it.id }) { m ->
                                    MenuRow(m)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onViewCart, enabled = CartStore.lines.isNotEmpty()) {
                Text("View Cart (${CartStore.lines.size}) • R${String.format("%.2f", CartStore.total())}")
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onToggle() }, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
    }
}

@Composable
private fun MenuRow(m: MenuItem) {
    Column(Modifier.fillMaxWidth()) {
        Text(m.name, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        Text(m.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("R${String.format("%.2f", m.price)}", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { CartStore.add(m, 1, null) }) { Text("Add") }
        }
    }
}

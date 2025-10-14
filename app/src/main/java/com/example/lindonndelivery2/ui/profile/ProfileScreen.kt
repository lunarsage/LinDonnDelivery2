package com.example.lindonndelivery2.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.model.OrderResponse
import com.example.lindonndelivery2.data.model.UserRow
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import com.example.lindonndelivery2.data.network.UsersService
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen() {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Account", "Orders", "Loyalty")
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }
        Spacer(Modifier.height(12.dp))
        when (tab) {
            0 -> AccountTab()
            1 -> OrdersTab()
            2 -> LoyaltyTab()
        }
    }
}

@Composable
private fun AccountTab() {
    val users = remember { ApiClient.rest.create(UsersService::class.java) }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserRow?>(null) }
    var address by remember { mutableStateOf("") }
    var notifEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        SessionManager.userId?.let { uid ->
            scope.launch {
                try {
                    val res = users.getById(idEq = "eq.$uid").firstOrNull()
                    user = res
                    address = res?.default_address ?: ""
                } catch (t: Throwable) { error = t.message }
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Text("Account Details", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Default address") },
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
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = notifEnabled, onCheckedChange = { notifEnabled = it })
            Spacer(Modifier.width(8.dp))
            Text("Push notifications")
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = marketingEnabled, onCheckedChange = { marketingEnabled = it })
            Spacer(Modifier.width(8.dp))
            Text("Marketing emails")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            SessionManager.userId?.let { uid ->
                scope.launch {
                    try {
                        users.upsert(listOf(UserRow(id = uid, email = user?.email ?: "", default_address = address)))
                        error = "Saved"
                    } catch (t: Throwable) { error = t.message }
                }
            }
        }) { Text("Save") }
        if (error != null) { Spacer(Modifier.height(6.dp)); Text(error!!, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun OrdersTab() {
    val ordersService = remember { ApiClient.rest.create(OrdersService::class.java) }
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        SessionManager.userId?.let { uid ->
            scope.launch {
                try {
                    orders = ordersService.listByUid(uidEq = "eq.$uid")
                } catch (t: Throwable) { error = t.message }
            }
        }
    }
    when {
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { o ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Order #${o.id.take(8)} — ${o.status}", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Total: R${String.format("%.2f", o.total)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Placed: ${o.created_at}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoyaltyTab() {
    val users = remember { ApiClient.rest.create(UsersService::class.java) }
    val scope = rememberCoroutineScope()
    var points by remember { mutableStateOf(0) }
    var balance by remember { mutableStateOf(0.0) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        SessionManager.userId?.let { uid ->
            scope.launch {
                try {
                    val row = users.getById(idEq = "eq.$uid").firstOrNull()
                    points = row?.points ?: 0
                    balance = row?.wallet_balance ?: 0.0
                } catch (t: Throwable) { error = t.message }
            }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Text("Loyalty Points: $points", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Wallet Balance: R${String.format("%.2f", balance)}", style = MaterialTheme.typography.bodyMedium)
        if (error != null) { Spacer(Modifier.height(6.dp)); Text(error!!, color = MaterialTheme.colorScheme.error) }
    }
}

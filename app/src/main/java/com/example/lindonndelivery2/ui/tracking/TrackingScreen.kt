package com.example.lindonndelivery2.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.model.OrderResponse
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import kotlinx.coroutines.delay

@Composable
fun TrackingScreen(orderId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val orders = remember { ApiClient.rest.create(OrdersService::class.java) }
    var order by remember { mutableStateOf<OrderResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        while (true) {
            try {
                val res = orders.getById(idEq = "eq.$orderId")
                order = res.firstOrNull()
                if (order?.status == "Delivered") break
            } catch (t: Throwable) {
                error = t.message
            }
            delay(4000)
        }
        onDone()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(context.getString(com.example.lindonndelivery2.R.string.order_tracking), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
        val s = order?.status ?: context.getString(com.example.lindonndelivery2.R.string.loading)
        Text(context.getString(com.example.lindonndelivery2.R.string.status, s), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(context.getString(com.example.lindonndelivery2.R.string.steps))
        val steps = listOf(
            context.getString(com.example.lindonndelivery2.R.string.confirmed),
            context.getString(com.example.lindonndelivery2.R.string.preparing),
            context.getString(com.example.lindonndelivery2.R.string.out_for_delivery),
            context.getString(com.example.lindonndelivery2.R.string.delivered)
        )
        steps.forEachIndexed { index, step ->
            // Match status by index position instead of string comparison for localization
            val currentStatusIndex = when (order?.status?.lowercase()) {
                context.getString(com.example.lindonndelivery2.R.string.confirmed).lowercase() -> 0
                context.getString(com.example.lindonndelivery2.R.string.preparing).lowercase() -> 1
                context.getString(com.example.lindonndelivery2.R.string.out_for_delivery).lowercase() -> 2
                context.getString(com.example.lindonndelivery2.R.string.delivered).lowercase() -> 3
                "confirmed" -> 0
                "preparing" -> 1
                "out for delivery" -> 2
                "delivered" -> 3
                else -> -1
            }
            val done = currentStatusIndex >= index
            Text("- ${if (done) "✓" else "•"} $step")
        }
    }
}

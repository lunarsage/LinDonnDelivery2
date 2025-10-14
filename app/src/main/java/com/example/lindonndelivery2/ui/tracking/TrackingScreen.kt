package com.example.lindonndelivery2.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.model.OrderResponse
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import kotlinx.coroutines.delay

@Composable
fun TrackingScreen(orderId: String, onDone: () -> Unit) {
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
        Text("Order Tracking", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
        val s = order?.status ?: "Loading..."
        Text("Status: $s", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Steps:")
        val steps = listOf("Confirmed","Preparing","Out for Delivery","Delivered")
        steps.forEach { step ->
            val done = steps.indexOf(step) <= steps.indexOf(order?.status ?: "Confirmed")
            Text("- ${if (done) "✓" else "•"} $step")
        }
    }
}

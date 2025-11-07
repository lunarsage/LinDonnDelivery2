package com.example.lindonndelivery2.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.cart.CartStore
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val context = LocalContext.current
    val lines = CartStore.lines
    val promoState = remember { mutableStateOf("") }
    val subtotal = CartStore.total()
    val discount = when (promoState.value.trim().uppercase()) {
        "SAVE10" -> subtotal * 0.10
        "LESS20" -> 20.0
        else -> 0.0
    }.coerceAtMost(subtotal)
    val delivery = CartStore.deliveryFee()
    val total = (subtotal - discount + delivery).coerceAtLeast(0.0)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(context.getString(com.example.lindonndelivery2.R.string.cart), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (lines.isEmpty()) {
            Text(context.getString(com.example.lindonndelivery2.R.string.cart_empty), style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(lines) { l ->
                    Column(Modifier.fillMaxWidth()) {
                        Text(l.item.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { CartStore.decrement(l.item.id) }) { Icon(Icons.Default.Remove, contentDescription = "Decrement") }
                            Text("${l.quantity}", style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { CartStore.increment(l.item.id) }) { Icon(Icons.Default.Add, contentDescription = "Increment") }
                            Spacer(Modifier.weight(1f))
                            Text("R${String.format("%.2f", l.item.price * l.quantity)}", style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { CartStore.remove(l.item.id) }) { Icon(Icons.Default.Delete, contentDescription = "Remove") }
                        }
                        if (!l.note.isNullOrBlank()) {
                            Text("Note: ${l.note}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextField(
                value = promoState.value,
                onValueChange = { promoState.value = it },
                label = { Text(context.getString(com.example.lindonndelivery2.R.string.promo_code)) },
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
            Spacer(Modifier.height(8.dp))
            Text(context.getString(com.example.lindonndelivery2.R.string.subtotal, String.format("%.2f", subtotal)), style = MaterialTheme.typography.bodyMedium)
            Text(context.getString(com.example.lindonndelivery2.R.string.discount, String.format("%.2f", discount)), style = MaterialTheme.typography.bodyMedium)
            Text(context.getString(com.example.lindonndelivery2.R.string.delivery, String.format("%.2f", delivery)), style = MaterialTheme.typography.bodyMedium)
            Text(context.getString(com.example.lindonndelivery2.R.string.total, String.format("%.2f", total)), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text(context.getString(com.example.lindonndelivery2.R.string.back)) }
            Button(onClick = onCheckout, enabled = lines.isNotEmpty()) { Text(context.getString(com.example.lindonndelivery2.R.string.proceed_to_checkout)) }
        }
    }
}

package com.example.lindonndelivery2.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.cart.CartStore
import com.example.lindonndelivery2.data.model.OrderCreate
import com.example.lindonndelivery2.data.model.OrderItem
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var promo by remember { mutableStateOf("") }
    var payingWithWallet by remember { mutableStateOf(true) }
    var placing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val subtotal = CartStore.total()
    val discount = when (promo.trim().uppercase()) {
        "SAVE10" -> subtotal * 0.10
        "LESS20" -> 20.0
        else -> 0.0
    }.coerceAtMost(subtotal)
    val total = (subtotal - discount).coerceAtLeast(0.0)

    val scope = rememberCoroutineScope()
    val orders = remember { ApiClient.rest.create(OrdersService::class.java) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Checkout", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Delivery address") },
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
            value = promo,
            onValueChange = { promo = it },
            label = { Text("Promo code (SAVE10 / LESS20)") },
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val selected = if (payingWithWallet) "Wallet" else "Card"
            OutlinedButton(onClick = { payingWithWallet = true }, enabled = !payingWithWallet) { Text("Wallet") }
            OutlinedButton(onClick = { payingWithWallet = false }, enabled = payingWithWallet) { Text("Card") }
            Text("$selected", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(12.dp))
        Text("Subtotal: R${String.format("%.2f", subtotal)}")
        Text("Discount: -R${String.format("%.2f", discount)}")
        Text("Total: R${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium)
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(
                onClick = {
                    scope.launch {
                        placing = true; error = null
                        try {
                            val uid = SessionManager.userId ?: throw IllegalStateException("Not logged in")
                            require(address.isNotBlank()) { "Enter address" }
                            val items = CartStore.lines.map { l ->
                                OrderItem(
                                    id = l.item.id,
                                    name = l.item.name,
                                    price = l.item.price,
                                    quantity = l.quantity,
                                    note = l.note
                                )
                            }
                            android.util.Log.d("CheckoutScreen", "Creating order for user: $uid")
                            android.util.Log.d("CheckoutScreen", "Order items: ${items.size}, Total: $total")
                            
                            val created = orders.create(
                                OrderCreate(
                                    uid = uid,
                                    items = items,
                                    total = total,
                                    address = address,
                                    status = "Confirmed"
                                )
                            )
                            val inserted = created.firstOrNull() ?: throw IllegalStateException("No order returned")
                            android.util.Log.d("CheckoutScreen", "Order created successfully: ${inserted.id}")
                            
                            // Verify FCM token is stored (notification trigger will use it)
                            com.example.lindonndelivery2.data.notifications.FcmTokenManager.getAndStoreToken()
                            
                            CartStore.clear()
                            onOrderPlaced(inserted.id)
                        } catch (t: Throwable) {
                            error = when (t) {
                                is HttpException -> {
                                    val body = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                                    if (t.code() == 409) {
                                        "Order conflict (409). Please review items/address and try again." + (body?.let { "\n$it" } ?: "")
                                    } else body ?: t.message
                                }
                                else -> t.message
                            }
                        } finally { placing = false }
                    }
                },
                enabled = !placing && CartStore.lines.isNotEmpty()
            ) { Text(if (placing) "Placing..." else "Place Order") }
        }
    }
}

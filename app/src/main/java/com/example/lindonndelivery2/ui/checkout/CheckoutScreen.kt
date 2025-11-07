package com.example.lindonndelivery2.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.lindonndelivery2.ui.theme.Grey300
import com.example.lindonndelivery2.ui.theme.Grey700
import com.example.lindonndelivery2.ui.theme.RustOrange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.cart.CartStore
import com.example.lindonndelivery2.data.model.OrderCreate
import com.example.lindonndelivery2.data.model.OrderItem
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import com.example.lindonndelivery2.util.LocalizedStrings
import com.example.lindonndelivery2.util.NotificationHelper
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
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(context.getString(com.example.lindonndelivery2.R.string.checkout), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(context.getString(com.example.lindonndelivery2.R.string.delivery_address)) },
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
            label = { Text(context.getString(com.example.lindonndelivery2.R.string.promo_code)) },
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
            val walletStr = context.getString(com.example.lindonndelivery2.R.string.wallet)
            val cardStr = context.getString(com.example.lindonndelivery2.R.string.card)
            val selected = if (payingWithWallet) walletStr else cardStr
            OutlinedButton(onClick = { payingWithWallet = true }, enabled = !payingWithWallet) { Text(walletStr) }
            OutlinedButton(onClick = { payingWithWallet = false }, enabled = payingWithWallet) { Text(cardStr) }
            Text(selected, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(12.dp))
        Text(context.getString(com.example.lindonndelivery2.R.string.subtotal, String.format("%.2f", subtotal)))
        Text(context.getString(com.example.lindonndelivery2.R.string.discount, String.format("%.2f", discount)))
        Text(context.getString(com.example.lindonndelivery2.R.string.total, String.format("%.2f", total)), style = MaterialTheme.typography.titleMedium)
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text(context.getString(com.example.lindonndelivery2.R.string.back)) }
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
                            
                            // Show immediate local notification
                            NotificationHelper.showNotification(
                                context = context,
                                title = context.getString(com.example.lindonndelivery2.R.string.order_confirmation),
                                message = context.getString(com.example.lindonndelivery2.R.string.order_confirmed_message),
                                orderId = inserted.id
                            )
                            
                            // Verify FCM token is stored (server-side notification trigger will use it)
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
            ) { Text(if (placing) context.getString(com.example.lindonndelivery2.R.string.placing) else context.getString(com.example.lindonndelivery2.R.string.place_order)) }
        }
    }
}

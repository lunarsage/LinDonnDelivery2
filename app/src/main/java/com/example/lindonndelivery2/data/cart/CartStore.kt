package com.example.lindonndelivery2.data.cart

import com.example.lindonndelivery2.data.model.MenuItem
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class CartLine(val item: MenuItem, var quantity: Int = 1, val note: String? = null)

object CartStore {
    private val _lines: SnapshotStateList<CartLine> = mutableStateListOf()
    val lines: SnapshotStateList<CartLine> get() = _lines

    fun add(item: MenuItem, qty: Int = 1, note: String? = null) {
        val idx = _lines.indexOfFirst { it.item.id == item.id && it.note == note }
        if (idx >= 0) _lines[idx].quantity += qty.coerceAtLeast(1)
        else _lines.add(CartLine(item, qty.coerceAtLeast(1), note))
    }

    fun increment(itemId: String) {
        val i = _lines.indexOfFirst { it.item.id == itemId }
        if (i >= 0) _lines[i].quantity += 1
    }

    fun decrement(itemId: String) {
        val i = _lines.indexOfFirst { it.item.id == itemId }
        if (i >= 0) {
            _lines[i].quantity -= 1
            if (_lines[i].quantity <= 0) _lines.removeAt(i)
        }
    }

    fun remove(itemId: String) {
        val i = _lines.indexOfFirst { it.item.id == itemId }
        if (i >= 0) _lines.removeAt(i)
    }

    fun clear() { _lines.clear() }

    fun total(): Double = _lines.sumOf { it.item.price * it.quantity }

    fun deliveryFee(): Double = if (_lines.isNotEmpty()) 20.0 else 0.0

    fun grandTotal(): Double = total() + deliveryFee()
}

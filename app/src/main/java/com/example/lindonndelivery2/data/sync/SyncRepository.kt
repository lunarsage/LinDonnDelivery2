package com.example.lindonndelivery2.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.lindonndelivery2.data.SessionManager
import com.example.lindonndelivery2.data.local.AppDatabase
import com.example.lindonndelivery2.data.local.entity.*
import com.example.lindonndelivery2.data.model.*
import com.example.lindonndelivery2.data.network.ApiClient
import com.example.lindonndelivery2.data.network.OrdersService
import com.example.lindonndelivery2.data.network.RestaurantService
import com.example.lindonndelivery2.data.network.MenuService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class SyncRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val restaurantDao = db.restaurantDao()
    private val menuItemDao = db.menuItemDao()
    private val orderDao = db.orderDao()
    private val cartDao = db.cartDao()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Restaurants
    suspend fun syncRestaurants(): Boolean {
        if (!isOnline()) return false
        return try {
            val service = ApiClient.rest.create(RestaurantService::class.java)
            val restaurants = service.list()
            
            val entities = restaurants.map { restaurant ->
                RestaurantEntity(
                    id = restaurant.id,
                    name = restaurant.name,
                    cuisine = restaurant.cuisine,
                    image_url = restaurant.image_url,
                    delivery_time = restaurant.delivery_time,
                    rating = restaurant.rating,
                    synced = true,
                    last_updated = System.currentTimeMillis()
                )
            }
            restaurantDao.insertAllRestaurants(entities)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getRestaurantsFlow(): Flow<List<RestaurantEntity>> = restaurantDao.getAllRestaurants()

    // Menu Items
    suspend fun syncMenuItems(restaurantId: String): Boolean {
        if (!isOnline()) return false
        return try {
            val service = ApiClient.rest.create(MenuService::class.java)
            val items = service.listByRestaurant(restaurantIdEq = "eq.$restaurantId")
            
            val entities = items.map { item ->
                MenuItemEntity(
                    id = item.id,
                    restaurant_id = item.restaurant_id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    image_url = item.image_url,
                    category = item.category,
                    synced = true,
                    last_updated = System.currentTimeMillis()
                )
            }
            menuItemDao.insertAllMenuItems(entities)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMenuItemsFlow(restaurantId: String): Flow<List<MenuItemEntity>> = 
        menuItemDao.getMenuItemsByRestaurant(restaurantId)

    // Orders
    suspend fun syncPendingOrders(): Boolean {
        if (!isOnline() || SessionManager.userId == null) return false
        return try {
            val pendingOrders = orderDao.getPendingSyncOrders()
            val ordersService = ApiClient.rest.create(OrdersService::class.java)
            
            pendingOrders.forEach { orderEntity ->
                try {
                    val itemsJson = JSONArray(orderEntity.items)
                    val items = mutableListOf<OrderItem>()
                    for (i in 0 until itemsJson.length()) {
                        val obj = itemsJson.getJSONObject(i)
                        items.add(OrderItem(
                            id = obj.optString("id", ""),
                            name = obj.optString("name", ""),
                            price = obj.getDouble("price"),
                            quantity = obj.getInt("quantity"),
                            note = obj.optString("note")
                        ))
                    }
                    
                    val orderRequest = OrderCreate(
                        uid = orderEntity.uid,
                        items = items,
                        total = orderEntity.total,
                        address = orderEntity.address,
                        status = orderEntity.status
                    )
                    
                    ordersService.create(orderRequest)
                    orderDao.markOrderSynced(orderEntity.id)
                } catch (e: Exception) {
                    // Skip failed order, try next
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveOrderOffline(order: OrderEntity) {
        orderDao.insertOrder(order)
    }

    fun getOrdersFlow(uid: String): Flow<List<OrderEntity>> = orderDao.getOrdersByUser(uid)

    // Cart
    suspend fun saveCartItem(item: CartEntity) {
        cartDao.insertCartItem(item)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    fun getCartItemsFlow(): Flow<List<CartEntity>> = cartDao.getAllCartItems()
}


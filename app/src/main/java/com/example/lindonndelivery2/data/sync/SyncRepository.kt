package com.example.lindonndelivery2.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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

/**
 * SyncRepository - Handles synchronization between local Room database and remote Supabase API
 * 
 * This class implements the offline-first architecture pattern:
 * 1. Data is stored locally in Room database (always available)
 * 2. When online, sync with remote Supabase API
 * 3. Changes are synchronized bidirectionally
 * 
 * Responsibilities:
 * - Sync restaurants from API to local database
 * - Sync menu items for specific restaurants
 * - Sync pending orders from local to remote (upload)
 * - Check network connectivity status
 * - Provide Flow-based reactive data streams
 * 
 * Architecture:
 * - Uses Room Database for local storage
 * - Uses Retrofit for API calls
 * - Uses Kotlin Flow for reactive data streams
 * - Handles network errors gracefully
 * 
 * Reference: Offline-first architecture - https://developer.android.com/topic/architecture/data-layer/offline-first
 */
private const val TAG = "SyncRepository"

class SyncRepository(context: Context) {
    // Room Database instance - provides local data storage
    private val db = AppDatabase.getDatabase(context)
    
    // Data Access Objects (DAOs) for database operations
    private val restaurantDao = db.restaurantDao()
    private val menuItemDao = db.menuItemDao()
    private val orderDao = db.orderDao()
    private val cartDao = db.cartDao()

    // ConnectivityManager for checking network status
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if device is online
     * 
     * Verifies network connectivity by checking:
     * 1. Active network exists
     * 2. Network has internet capability
     * 3. Network is validated (can actually reach internet)
     * 
     * This is more reliable than just checking if network is connected,
     * as it ensures the network can actually reach the internet.
     * 
     * @return true if device has validated internet connection, false otherwise
     * 
     * Reference: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
     */
    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork
        if (network == null) {
            Log.d(TAG, "No active network - device is offline")
            return false
        }
        
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            Log.d(TAG, "No network capabilities - device is offline")
            return false
        }
        
        val isOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        Log.d(TAG, "Network status: ${if (isOnline) "online" else "offline"}")
        return isOnline
    }

    /**
     * Sync Restaurants
     * 
     * Fetches restaurants from Supabase API and stores them in local database
     * This enables offline access to restaurant listings
     * 
     * Process:
     * 1. Check if device is online
     * 2. Fetch restaurants from API
     * 3. Convert API models to Room entities
     * 4. Insert/update in local database
     * 5. Mark as synced with timestamp
     * 
     * @return true if sync successful, false otherwise
     */
    suspend fun syncRestaurants(): Boolean {
        if (!isOnline()) {
            Log.w(TAG, "Cannot sync restaurants - device is offline")
            return false
        }
        
        return try {
            Log.d(TAG, "Starting restaurant sync")
            val service = ApiClient.rest.create(RestaurantService::class.java)
            val restaurants = service.list()
            Log.d(TAG, "Fetched ${restaurants.size} restaurants from API")
            
            // Convert API models to Room entities
            val entities = restaurants.map { restaurant ->
                RestaurantEntity(
                    id = restaurant.id,
                    name = restaurant.name,
                    cuisine = restaurant.cuisine,
                    image_url = restaurant.image_url,
                    delivery_time = restaurant.delivery_time,
                    rating = restaurant.rating,
                    synced = true,  // Mark as synced
                    last_updated = System.currentTimeMillis()  // Update timestamp
                )
            }
            
            // Insert into local database (Room handles conflicts with @Insert(onConflict = REPLACE))
            restaurantDao.insertAllRestaurants(entities)
            Log.d(TAG, "Restaurant sync completed successfully - ${entities.size} restaurants stored")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync restaurants", e)
            false
        }
    }

    fun getRestaurantsFlow(): Flow<List<RestaurantEntity>> = restaurantDao.getAllRestaurants()

    /**
     * Sync Menu Items
     * 
     * Fetches menu items for a specific restaurant from Supabase API
     * and stores them in local database
     * 
     * Process:
     * 1. Check if device is online
     * 2. Fetch menu items for restaurant from API
     * 3. Convert API models to Room entities
     * 4. Insert/update in local database
     * 5. Mark as synced with timestamp
     * 
     * @param restaurantId ID of the restaurant to sync menu items for
     * @return true if sync successful, false otherwise
     */
    suspend fun syncMenuItems(restaurantId: String): Boolean {
        if (!isOnline()) {
            Log.w(TAG, "Cannot sync menu items for restaurant $restaurantId - device is offline")
            return false
        }
        
        return try {
            Log.d(TAG, "Starting menu items sync for restaurant: $restaurantId")
            val service = ApiClient.rest.create(MenuService::class.java)
            // PostgREST query: filter by restaurant_id using "eq" operator
            val items = service.listByRestaurant(restaurantIdEq = "eq.$restaurantId")
            Log.d(TAG, "Fetched ${items.size} menu items from API")
            
            // Convert API models to Room entities
            val entities = items.map { item ->
                MenuItemEntity(
                    id = item.id,
                    restaurant_id = item.restaurant_id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    image_url = item.image_url,
                    category = item.category,
                    synced = true,  // Mark as synced
                    last_updated = System.currentTimeMillis()  // Update timestamp
                )
            }
            
            // Insert into local database
            menuItemDao.insertAllMenuItems(entities)
            Log.d(TAG, "Menu items sync completed successfully - ${entities.size} items stored")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync menu items for restaurant $restaurantId", e)
            false
        }
    }

    fun getMenuItemsFlow(restaurantId: String): Flow<List<MenuItemEntity>> = 
        menuItemDao.getMenuItemsByRestaurant(restaurantId)

    /**
     * Sync Pending Orders
     * 
     * Uploads locally created orders (created while offline) to Supabase API
     * This is the "upload" part of the sync process
     * 
     * Process:
     * 1. Check if device is online and user is logged in
     * 2. Get all pending orders (synced = false) from local database
     * 3. For each pending order:
     *    - Parse JSON items array
     *    - Convert to API model
     *    - Upload to API
     *    - Mark as synced in local database
     * 4. Continue even if individual orders fail (resilient sync)
     * 
     * This enables offline order creation - orders are stored locally
     * and uploaded when connectivity is restored
     * 
     * @return true if sync process completed (even if some orders failed), false on critical error
     */
    suspend fun syncPendingOrders(): Boolean {
        if (!isOnline()) {
            Log.w(TAG, "Cannot sync pending orders - device is offline")
            return false
        }
        
        if (SessionManager.userId == null) {
            Log.w(TAG, "Cannot sync pending orders - user not logged in")
            return false
        }
        
        return try {
            Log.d(TAG, "Starting pending orders sync")
            val pendingOrders = orderDao.getPendingSyncOrders()
            Log.d(TAG, "Found ${pendingOrders.size} pending orders to sync")
            
            if (pendingOrders.isEmpty()) {
                Log.d(TAG, "No pending orders to sync")
                return true
            }
            
            val ordersService = ApiClient.rest.create(OrdersService::class.java)
            var successCount = 0
            var failCount = 0
            
            // Process each pending order
            pendingOrders.forEach { orderEntity ->
                try {
                    Log.d(TAG, "Syncing order: ${orderEntity.id}")
                    
                    // Parse JSON items array stored in database
                    val itemsJson = JSONArray(orderEntity.items)
                    val items = mutableListOf<OrderItem>()
                    
                    // Convert JSON objects to OrderItem models
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
                    
                    // Create API request model
                    val orderRequest = OrderCreate(
                        uid = orderEntity.uid,
                        items = items,
                        total = orderEntity.total,
                        address = orderEntity.address,
                        status = orderEntity.status
                    )
                    
                    // Upload to API
                    ordersService.create(orderRequest)
                    
                    // Mark as synced in local database
                    orderDao.markOrderSynced(orderEntity.id)
                    successCount++
                    Log.d(TAG, "Order ${orderEntity.id} synced successfully")
                } catch (e: Exception) {
                    // Continue with next order even if this one fails
                    failCount++
                    Log.e(TAG, "Failed to sync order ${orderEntity.id}", e)
                }
            }
            
            Log.d(TAG, "Pending orders sync completed - Success: $successCount, Failed: $failCount")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during pending orders sync", e)
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


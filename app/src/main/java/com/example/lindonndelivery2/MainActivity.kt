package com.example.lindonndelivery2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import com.example.lindonndelivery2.ui.auth.LoginScreen
import com.example.lindonndelivery2.ui.restaurants.RestaurantsScreen
import com.example.lindonndelivery2.ui.menu.MenuScreen
import com.example.lindonndelivery2.ui.cart.CartScreen
import com.example.lindonndelivery2.ui.checkout.CheckoutScreen
import com.example.lindonndelivery2.ui.tracking.TrackingScreen
import com.example.lindonndelivery2.ui.theme.LinDonnDelivery2Theme
import com.example.lindonndelivery2.ui.profile.ProfileScreen
import com.example.lindonndelivery2.ui.settings.SettingsScreen
import com.example.lindonndelivery2.util.LocaleHelper

/**
 * MainActivity - Entry point of the LinDonn Delivery 2 application
 * 
 * This class handles:
 * 1. Application initialization and locale setup
 * 2. Authentication state management
 * 3. Navigation between screens using a sealed class Screen hierarchy
 * 4. Bottom navigation bar with Material 3 design
 * 
 * Architecture:
 * - Uses Jetpack Compose for declarative UI
 * - State-based navigation (no Navigation Component library)
 * - Sealed class pattern for type-safe screen routing
 * - In-memory state management for demo purposes
 * 
 * Navigation Flow:
 * - Unauthenticated: Shows LoginScreen
 * - Authenticated: Shows main app with bottom navigation (Home, Cart, Profile)
 * - Screen transitions handled via state changes in sealed Screen class
 */
private const val TAG = "MainActivity"

/**
 * Sealed class representing all possible screens in the app
 * This provides type-safe navigation and prevents invalid screen states
 * 
 * Each screen can be:
 * - A simple object (Restaurants, Cart, Checkout, Profile, Settings)
 * - A data class with parameters (Menu with restaurant info, Tracking with orderId)
 */
sealed class Screen {
    data object Restaurants: Screen()  // Main restaurant listing screen
    data class Menu(val id: String, val name: String): Screen()  // Menu for specific restaurant
    data object Cart: Screen()  // Shopping cart screen
    data object Checkout: Screen()  // Checkout and payment screen
    data class Tracking(val orderId: String): Screen()  // Order tracking screen
    data object Profile: Screen()  // User profile screen
    data object Settings: Screen()  // User settings screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity created - initializing application")
        
        /**
         * Language/Locale Setup
         * 
         * Restores the user's saved language preference and applies it to the app
         * This ensures the app displays in the correct language even after restart
         * 
         * Process:
         * 1. Get saved language from SharedPreferences via LocaleHelper
         * 2. Create Locale object with the saved language code
         * 3. Set as default locale and update configuration
         * 4. This affects all string resources and locale-dependent formatting
         */
        val language = LocaleHelper.getSavedLanguage(this)
        Log.d(TAG, "Loaded saved language preference: $language")
        val config = resources.configuration
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Enable edge-to-edge display for modern Android design
        enableEdgeToEdge()
        
        /**
         * Compose Content Setup
         * 
         * Sets up the Compose UI hierarchy:
         * 1. Theme wrapper (LinDonnDelivery2Theme) - provides Material 3 theming
         * 2. Authentication state - determines if user is logged in
         * 3. Screen navigation state - tracks current screen
         * 4. Conditional rendering - shows LoginScreen or main app based on auth state
         */
        setContent {
            LinDonnDelivery2Theme {
                // Authentication state: false = show login, true = show main app
                var authed by remember { mutableStateOf(false) }
                
                // Current screen state: tracks which screen is currently displayed
                // Default to Restaurants screen when app starts
                var screen by remember { mutableStateOf<Screen>(Screen.Restaurants) }

                // Conditional rendering based on authentication state
                if (!authed) {
                    /**
                     * Login Screen
                     * 
                     * Displayed when user is not authenticated
                     * onSuccess callback sets authed = true, triggering navigation to main app
                     */
                    Log.d(TAG, "User not authenticated - showing LoginScreen")
                    LoginScreen(onSuccess = { 
                        Log.d(TAG, "Login successful - navigating to main app")
                        authed = true 
                    })
                } else {
                    /**
                     * Main Application UI
                     * 
                     * Scaffold provides:
                     * - Bottom navigation bar (always visible)
                     * - Content padding to prevent overlap with system bars
                     * - Material 3 design system integration
                     */
                    Log.d(TAG, "User authenticated - showing main app with screen: ${screen::class.simpleName}")
                    
                    Scaffold(
                        bottomBar = {
                            /**
                             * Bottom Navigation Bar
                             * 
                             * Provides three main navigation tabs:
                             * 1. Home - Restaurants and Menu screens
                             * 2. Cart - Shopping cart and Checkout screens
                             * 3. Profile - User profile and Settings screens
                             * 
                             * Selected state is determined by current screen type
                             */
                            NavigationBar {
                                // Home tab: selected when on Restaurants or Menu screen
                                NavigationBarItem(
                                    selected = screen is Screen.Restaurants || screen is Screen.Menu,
                                    onClick = { 
                                        Log.d(TAG, "Navigation: Home tab clicked")
                                        screen = Screen.Restaurants 
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                // Cart tab: selected when on Cart or Checkout screen
                                NavigationBarItem(
                                    selected = screen is Screen.Cart || screen is Screen.Checkout,
                                    onClick = { 
                                        Log.d(TAG, "Navigation: Cart tab clicked")
                                        screen = Screen.Cart 
                                    },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                                    label = { Text("Cart") }
                                )
                                // Profile tab: selected when on Profile or Settings screen
                                NavigationBarItem(
                                    selected = screen is Screen.Profile || screen is Screen.Settings,
                                    onClick = { 
                                        Log.d(TAG, "Navigation: Profile tab clicked")
                                        screen = Screen.Profile 
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        /**
                         * Screen Content Area
                         * 
                         * Uses when expression for type-safe screen routing
                         * Each screen receives appropriate parameters and callbacks
                         * 
                         * Navigation flow:
                         * - Restaurants → Menu (when restaurant clicked)
                         * - Menu → Cart (when view cart clicked)
                         * - Cart → Checkout (when checkout clicked)
                         * - Checkout → Tracking (when order placed)
                         * - Profile → Settings (when settings clicked)
                         */
                        Box(Modifier.padding(innerPadding)) {
                            when (val s = screen) {
                                is Screen.Restaurants -> {
                                    Log.d(TAG, "Displaying RestaurantsScreen")
                                    RestaurantsScreen(onRestaurantClick = { r ->
                                        Log.d(TAG, "Restaurant clicked: ${r.name} (${r.id})")
                                        screen = Screen.Menu(r.id, r.name)
                                    })
                                }
                                is Screen.Menu -> {
                                    Log.d(TAG, "Displaying MenuScreen for restaurant: ${s.name}")
                                    MenuScreen(
                                        restaurantId = s.id,
                                        restaurantName = s.name,
                                        onBack = { 
                                            Log.d(TAG, "MenuScreen: Back to Restaurants")
                                            screen = Screen.Restaurants 
                                        },
                                        onViewCart = { 
                                            Log.d(TAG, "MenuScreen: Navigate to Cart")
                                            screen = Screen.Cart 
                                        }
                                    )
                                }
                                is Screen.Cart -> {
                                    Log.d(TAG, "Displaying CartScreen")
                                    CartScreen(
                                        onBack = { 
                                            Log.d(TAG, "CartScreen: Back to Restaurants")
                                            screen = Screen.Restaurants 
                                        },
                                        onCheckout = { 
                                            Log.d(TAG, "CartScreen: Navigate to Checkout")
                                            screen = Screen.Checkout 
                                        }
                                    )
                                }
                                is Screen.Checkout -> {
                                    Log.d(TAG, "Displaying CheckoutScreen")
                                    CheckoutScreen(
                                        onBack = { 
                                            Log.d(TAG, "CheckoutScreen: Back to Cart")
                                            screen = Screen.Cart 
                                        },
                                        onOrderPlaced = { orderId -> 
                                            Log.d(TAG, "CheckoutScreen: Order placed with ID: $orderId")
                                            screen = Screen.Tracking(orderId) 
                                        }
                                    )
                                }
                                is Screen.Tracking -> {
                                    Log.d(TAG, "Displaying TrackingScreen for order: ${s.orderId}")
                                    TrackingScreen(orderId = s.orderId) {
                                        Log.d(TAG, "TrackingScreen: Back to Restaurants")
                                        screen = Screen.Restaurants
                                    }
                                }
                                is Screen.Profile -> {
                                    Log.d(TAG, "Displaying ProfileScreen")
                                    ProfileScreen(onNavigateToSettings = {
                                        Log.d(TAG, "ProfileScreen: Navigate to Settings")
                                        screen = Screen.Settings
                                    })
                                }
                                is Screen.Settings -> {
                                    Log.d(TAG, "Displaying SettingsScreen")
                                    SettingsScreen(onBack = {
                                        Log.d(TAG, "SettingsScreen: Back to Profile")
                                        screen = Screen.Profile
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Log.d(TAG, "MainActivity initialization complete")
    }
}
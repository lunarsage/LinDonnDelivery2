package com.example.lindonndelivery2

import android.os.Bundle
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

sealed class Screen {
    data object Restaurants: Screen()
    data class Menu(val id: String, val name: String): Screen()
    data object Cart: Screen()
    data object Checkout: Screen()
    data class Tracking(val orderId: String): Screen()
    data object Profile: Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinDonnDelivery2Theme {
                var authed by remember { mutableStateOf(false) }
                var screen by remember { mutableStateOf<Screen>(Screen.Restaurants) }

                if (!authed) {
                    LoginScreen(onSuccess = { authed = true })
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = screen is Screen.Restaurants || screen is Screen.Menu,
                                    onClick = { screen = Screen.Restaurants },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = screen is Screen.Cart || screen is Screen.Checkout,
                                    onClick = { screen = Screen.Cart },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                                    label = { Text("Cart") }
                                )
                                NavigationBarItem(
                                    selected = screen is Screen.Profile,
                                    onClick = { screen = Screen.Profile },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) {
                            when (val s = screen) {
                            is Screen.Restaurants -> RestaurantsScreen(onRestaurantClick = { r ->
                                screen = Screen.Menu(r.id, r.name)
                            })
                            is Screen.Menu -> MenuScreen(
                                restaurantId = s.id,
                                restaurantName = s.name,
                                onBack = { screen = Screen.Restaurants },
                                onViewCart = { screen = Screen.Cart }
                            )
                            is Screen.Cart -> CartScreen(
                                onBack = { screen = Screen.Restaurants },
                                onCheckout = { screen = Screen.Checkout }
                            )
                            is Screen.Checkout -> CheckoutScreen(
                                onBack = { screen = Screen.Cart },
                                onOrderPlaced = { orderId -> screen = Screen.Tracking(orderId) }
                            )
                            is Screen.Tracking -> TrackingScreen(orderId = s.orderId) {
                                screen = Screen.Restaurants
                            }
                            is Screen.Profile -> ProfileScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
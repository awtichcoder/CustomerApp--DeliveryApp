package com.deliveryapp.customer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deliveryapp.customer.feature.auth.LoginScreen
import com.deliveryapp.customer.feature.auth.RegisterScreen
import com.deliveryapp.customer.feature.home.HomeScreen
import com.deliveryapp.customer.feature.order.CreateOrderScreen
import com.deliveryapp.customer.feature.order.OrderDetailScreen
import com.deliveryapp.customer.feature.order.OrdersScreen
import com.deliveryapp.customer.feature.profile.ProfileScreen
import com.deliveryapp.customer.feature.rewards.RewardsScreen
import com.deliveryapp.customer.feature.tracking.TrackingScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    fun openMain(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(AppRoutes.Home) { inclusive = route == AppRoutes.Home }
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.Login) {
        composable(AppRoutes.Login) {
            LoginScreen(
                onLogin = {
                    navController.navigate(AppRoutes.Home) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(AppRoutes.Register) }
            )
        }
        composable(AppRoutes.Register) {
            RegisterScreen(onRegistered = { navController.popBackStack() })
        }
        composable(AppRoutes.Home) {
            HomeScreen(
                onCreateOrder = { navController.navigate(AppRoutes.CreateOrder) },
                onOrders = { openMain(AppRoutes.Orders) },
                onRewards = { openMain(AppRoutes.Rewards) },
                onProfile = { openMain(AppRoutes.Profile) }
            )
        }
        composable(AppRoutes.CreateOrder) {
            CreateOrderScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.navigate(AppRoutes.OrderDetail) }
            )
        }
        composable(AppRoutes.OrderDetail) {
            OrderDetailScreen(
                onBack = { navController.popBackStack() },
                onTrack = { navController.navigate(AppRoutes.Tracking) },
                onCancelled = { openMain(AppRoutes.Orders) }
            )
        }
        composable(AppRoutes.Tracking) {
            TrackingScreen(onBack = { openMain(AppRoutes.Orders) })
        }
        composable(AppRoutes.Orders) {
            OrdersScreen(
                onTrack = { navController.navigate(AppRoutes.Tracking) },
                onHome = { openMain(AppRoutes.Home) },
                onRewards = { openMain(AppRoutes.Rewards) },
                onProfile = { openMain(AppRoutes.Profile) }
            )
        }
        composable(AppRoutes.Rewards) {
            RewardsScreen(
                onBack = { openMain(AppRoutes.Home) },
                onUsePoints = { navController.navigate(AppRoutes.CreateOrder) }
            )
        }
        composable(AppRoutes.Profile) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(AppRoutes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHome = { openMain(AppRoutes.Home) },
                onOrders = { openMain(AppRoutes.Orders) },
                onRewards = { openMain(AppRoutes.Rewards) }
            )
        }
    }
}

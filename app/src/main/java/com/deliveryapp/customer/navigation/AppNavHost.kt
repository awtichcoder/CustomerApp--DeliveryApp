package com.deliveryapp.customer.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deliveryapp.customer.DeliveryCustomerApplication
import com.deliveryapp.customer.feature.auth.AuthViewModel
import com.deliveryapp.customer.feature.auth.LoginScreen
import com.deliveryapp.customer.feature.auth.RegisterScreen
import com.deliveryapp.customer.feature.auth.SessionCheckScreen
import com.deliveryapp.customer.feature.auth.SessionViewModel
import com.deliveryapp.customer.feature.home.HomeScreen
import com.deliveryapp.customer.feature.home.HomeViewModel
import com.deliveryapp.customer.feature.order.CreateOrderScreen
import com.deliveryapp.customer.feature.order.OrderDetailScreen
import com.deliveryapp.customer.feature.order.OrdersScreen
import com.deliveryapp.customer.feature.profile.ProfileScreen
import com.deliveryapp.customer.feature.profile.ProfileViewModel
import com.deliveryapp.customer.feature.rewards.RewardsScreen
import com.deliveryapp.customer.feature.rewards.RewardsViewModel
import com.deliveryapp.customer.feature.tracking.TrackingScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    val container = (LocalContext.current.applicationContext as DeliveryCustomerApplication).container

    fun openLoginCleared() {
        navController.navigate(AppRoutes.Login) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }

    fun openMain(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(AppRoutes.Home) { inclusive = false }
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.Session) {
        composable(AppRoutes.Session) {
            val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.factory(container))
            val state = sessionViewModel.state
            LaunchedEffect(state.isChecking, state.isAuthenticated) {
                if (!state.isChecking) {
                    navController.navigate(if (state.isAuthenticated) AppRoutes.Home else AppRoutes.Login) {
                        popUpTo(AppRoutes.Session) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            SessionCheckScreen()
        }
        composable(AppRoutes.Login) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(container))
            LoginScreen(
                viewModel = authViewModel,
                onLogin = {
                    navController.navigate(AppRoutes.Home) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegister = { navController.navigate(AppRoutes.Register) }
            )
        }
        composable(AppRoutes.Register) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(container))
            RegisterScreen(
                viewModel = authViewModel,
                onRegistered = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.Home) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
            HomeScreen(
                viewModel = homeViewModel,
                onCreateOrder = { navController.navigate(AppRoutes.CreateOrder) },
                onOrders = { openMain(AppRoutes.Orders) },
                onRewards = { openMain(AppRoutes.Rewards) },
                onProfile = { openMain(AppRoutes.Profile) },
                onSessionExpired = { openLoginCleared() }
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
            val rewardsViewModel: RewardsViewModel = viewModel(factory = RewardsViewModel.factory(container))
            RewardsScreen(
                viewModel = rewardsViewModel,
                onBack = { openMain(AppRoutes.Home) },
                onUsePoints = { navController.navigate(AppRoutes.CreateOrder) },
                onSessionExpired = { openLoginCleared() }
            )
        }
        composable(AppRoutes.Profile) {
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(container))
            ProfileScreen(
                viewModel = profileViewModel,
                onLogout = { openLoginCleared() },
                onHome = { openMain(AppRoutes.Home) },
                onOrders = { openMain(AppRoutes.Orders) },
                onRewards = { openMain(AppRoutes.Rewards) },
                onSessionExpired = { openLoginCleared() }
            )
        }
    }
}

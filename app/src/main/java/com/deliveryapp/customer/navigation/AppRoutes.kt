package com.deliveryapp.customer.navigation

object AppRoutes {
    const val Session = "session"
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val CreateOrder = "create-order"
    const val OrderDetail = "order-detail/{orderId}"
    const val Tracking = "tracking/{orderId}"
    const val Orders = "orders"
    const val Rewards = "rewards"
    const val Profile = "profile"
    const val Notifications = "notifications"

    fun orderDetail(orderId: String) = "order-detail/$orderId"
    fun tracking(orderId: String) = "tracking/$orderId"
}

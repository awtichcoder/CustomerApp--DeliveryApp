package com.deliveryapp.customer

import android.content.Context
import com.deliveryapp.customer.core.network.NetworkFactory
import com.deliveryapp.customer.core.storage.TokenManager
import com.deliveryapp.customer.data.api.DeliveryApi
import com.deliveryapp.customer.data.api.IdentityApi
import com.deliveryapp.customer.data.api.LoyaltyApi
import com.deliveryapp.customer.data.api.OrderApi

class AppContainer(context: Context) {
    val tokenManager = TokenManager(context.applicationContext)

    private val retrofit = NetworkFactory.create(tokenManager)

    val identityApi: IdentityApi = retrofit.create(IdentityApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
    val deliveryApi: DeliveryApi = retrofit.create(DeliveryApi::class.java)
    val loyaltyApi: LoyaltyApi = retrofit.create(LoyaltyApi::class.java)
}

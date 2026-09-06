package com.deliveryapp.customer

import android.content.Context
import com.deliveryapp.customer.core.network.NetworkFactory
import com.deliveryapp.customer.core.storage.TokenManager
import com.deliveryapp.customer.data.api.DeliveryApi
import com.deliveryapp.customer.data.api.AddressSearchNetworkFactory
import com.deliveryapp.customer.data.api.IdentityApi
import com.deliveryapp.customer.data.api.LoyaltyApi
import com.deliveryapp.customer.data.api.OrderApi
import com.deliveryapp.customer.data.api.RoutingNetworkFactory
import com.deliveryapp.customer.data.repository.AuthRepository
import com.deliveryapp.customer.data.repository.LoyaltyRepository
import com.deliveryapp.customer.data.repository.ProfileRepository
import com.deliveryapp.customer.feature.order.NetworkOrderRepository
import com.deliveryapp.customer.feature.order.NetworkAddressRepository
import com.deliveryapp.customer.feature.order.OrderRepository
import com.deliveryapp.customer.feature.tracking.DeliveryRepository
import com.deliveryapp.customer.feature.tracking.NetworkDeliveryRepository
import com.deliveryapp.customer.feature.tracking.NetworkRoutingRepository
import com.deliveryapp.customer.feature.tracking.RoutingRepository

class AppContainer(context: Context) {
    val tokenManager = TokenManager(context.applicationContext)

    private val retrofit = NetworkFactory.create(tokenManager)

    val identityApi: IdentityApi = retrofit.create(IdentityApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
    val deliveryApi: DeliveryApi = retrofit.create(DeliveryApi::class.java)
    val loyaltyApi: LoyaltyApi = retrofit.create(LoyaltyApi::class.java)
    private val addressSearchApi = AddressSearchNetworkFactory.create()
    private val routingApi = RoutingNetworkFactory.create()

    val orderRepository: OrderRepository = NetworkOrderRepository(orderApi)
    val addressRepository = NetworkAddressRepository(addressSearchApi)
    val deliveryRepository: DeliveryRepository = NetworkDeliveryRepository(deliveryApi)
    val routingRepository: RoutingRepository = NetworkRoutingRepository(routingApi)
    val authRepository = AuthRepository(identityApi, tokenManager)
    val profileRepository = ProfileRepository(identityApi)
    val loyaltyRepository = LoyaltyRepository(loyaltyApi)
}

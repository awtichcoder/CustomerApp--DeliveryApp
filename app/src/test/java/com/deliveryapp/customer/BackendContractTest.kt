package com.deliveryapp.customer

import com.deliveryapp.customer.core.network.ApiResult
import com.deliveryapp.customer.data.api.*
import com.deliveryapp.customer.data.dto.*
import com.deliveryapp.customer.feature.order.NetworkCheckoutRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

/** Opt-in check against local Docker using the application's actual Retrofit/Gson DTOs. */
class BackendContractTest {
    @Test
    fun customerDtos_decodeLiveBackendAndCheckoutPreview() {
        assumeTrue("Set RUN_BACKEND_CONTRACT=1 with local Docker running", System.getenv("RUN_BACKEND_CONTRACT") == "1")
        runBlocking {
            fun retrofit(client: OkHttpClient) = Retrofit.Builder().baseUrl("http://localhost:6200/")
                .client(client).addConverterFactory(GsonConverterFactory.create()).build()
            val identity = retrofit(OkHttpClient()).create(IdentityApi::class.java)
            val token = identity.login(LoginRequest(
                System.getenv("TEST_CUSTOMER_EMAIL") ?: "dev@deliveryapp.local",
                System.getenv("TEST_CUSTOMER_PASSWORD") ?: "Dev123!"))
            try {
                val client = OkHttpClient.Builder().addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer ${token.accessToken}").build())
                }.build()
                val api = retrofit(client)
                val loyalty = api.create(LoyaltyApi::class.java)
                val orders = api.create(OrderApi::class.java)
                val promotions = api.create(PromotionApi::class.java)
                val deliveryApi = api.create(DeliveryApi::class.java)
                val account = loyalty.getMe()
                assertTrue(account.availablePoints <= account.points)
                assertTrue(account.tierDiscountPercent in listOf(0, 5, 10))
                assertNotNull(account.recentTransactions)
                val active = promotions.getActive()
                assertTrue(active.isNotEmpty())
                assertTrue(active.all { it.code.isNotBlank() && it.endAt.isNotBlank() })
                val order = orders.getOrders(status = "COMPLETED").items.first()
                val delivery = deliveryApi.getByOrder(order.orderId)
                assertTrue(delivery.pickupLatitude in 1.0..90.0)
                assertTrue(delivery.pickupLongitude in 1.0..180.0)
                assertTrue(delivery.dropoffLatitude in 1.0..90.0)
                assertTrue(delivery.dropoffLongitude in 1.0..180.0)
                val result = NetworkCheckoutRepository(orders, loyalty, promotions).preview(CreateOrderRequest(
                    "Contract pickup", "Contract dropoff", GeoPointRequest(10.776889, 106.700806),
                    GeoPointRequest(10.762622, 106.660172), "0912345678", null, 0, UUID.randomUUID().toString()))
                assertTrue(result is ApiResult.Success)
                val preview = (result as ApiResult.Success).data
                assertTrue(preview.price.shippingFee > 0)
                assertEquals(preview.price.shippingFee, preview.totalFee)
                assertEquals(0, preview.points.usablePoints)
            } finally {
                identity.logout(LogoutRequest(token.refreshToken))
            }
        }
    }
}

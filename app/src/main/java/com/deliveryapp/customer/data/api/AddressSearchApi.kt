package com.deliveryapp.customer.data.api

import com.deliveryapp.customer.data.dto.PhotonResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AddressSearchApi {
    @GET("api/")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("countrycode") countryCode: String = "VN",
        @Query("lat") latitude: Double? = null,
        @Query("lon") longitude: Double? = null
    ): PhotonResponse

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1
    ): PhotonResponse
}

object AddressSearchNetworkFactory {
    fun create(): AddressSearchApi {
        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "DeliveryCustomer/1.0 (student-project)")
                        .build()
                )
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://photon.komoot.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AddressSearchApi::class.java)
    }
}

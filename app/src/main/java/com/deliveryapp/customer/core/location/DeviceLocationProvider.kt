package com.deliveryapp.customer.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

object DeviceLocationProvider {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = suspendCancellableCoroutine { continuation ->
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val candidates = if (hasFineLocation) {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER)
        }
        val providers = candidates
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
        val cachedLocation = candidates
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)

        if (providers.isEmpty()) {
            continuation.resume(cachedLocation)
            return@suspendCancellableCoroutine
        }

        val completed = AtomicBoolean(false)
        val handler = Handler(Looper.getMainLooper())
        lateinit var listener: LocationListener

        fun finish(location: Location?) {
            if (!completed.compareAndSet(false, true)) return
            handler.removeCallbacksAndMessages(null)
            runCatching { manager.removeUpdates(listener) }
            if (continuation.isActive) continuation.resume(location)
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = finish(location)
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
            @Deprecated("Deprecated in Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        val provider = providers.first()
        runCatching {
            manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            handler.postDelayed({ finish(cachedLocation) }, LOCATION_TIMEOUT_MS)
        }.onFailure { finish(cachedLocation) }

        continuation.invokeOnCancellation {
            if (completed.compareAndSet(false, true)) {
                handler.removeCallbacksAndMessages(null)
                runCatching { manager.removeUpdates(listener) }
            }
        }
    }
}

private const val LOCATION_TIMEOUT_MS = 10_000L

package com.deliveryapp.customer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DeliveryGreen,
    secondary = DeliveryTextSecondary,
    primaryContainer = DeliveryGreenDark,
    background = DeliveryTextPrimary,
    surface = DeliveryTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = DeliveryGreen,
    onPrimary = Color.White,
    primaryContainer = DeliveryGreenSoft,
    onPrimaryContainer = DeliveryGreenDark,
    secondary = DeliveryTextSecondary,
    background = DeliveryCanvas,
    onBackground = DeliveryTextPrimary,
    surface = Color.White,
    onSurface = DeliveryTextPrimary,
    surfaceVariant = DeliveryGreenSoft,
    onSurfaceVariant = DeliveryTextSecondary,
    outline = DeliveryBorder
)

@Composable
fun DeliveryCustomerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

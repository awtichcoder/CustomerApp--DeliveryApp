package com.deliveryapp.customer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DeliveryGreen,
    secondary = DeliveryGreenSoft,
    tertiary = DeliveryWarning,
    background = DeliveryCanvas,
    surface = DeliverySurface,
    onPrimary = Color.White,
    onSecondary = DeliveryText,
    onTertiary = DeliveryText,
    onBackground = DeliveryText,
    onSurface = DeliveryText,
    surfaceVariant = DeliveryGreenSoft,
    onSurfaceVariant = DeliveryMuted,
    outline = DeliveryBorder,
    error = DeliveryDanger
)

@Composable
fun DeliveryCustomerTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

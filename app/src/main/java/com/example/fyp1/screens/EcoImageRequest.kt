package com.example.fyp1.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import java.io.File

@Composable
internal fun rememberEcoImageRequest(imageUrl: String?): ImageRequest? {
    val context = LocalContext.current
    val cleanUrl = imageUrl?.trim()?.takeIf { it.isNotEmpty() }
    return remember(context, cleanUrl) {
        cleanUrl?.let { url ->
            val imageData: Any = if (url.startsWith("/") || url.startsWith("file://")) {
                File(url.removePrefix("file://"))
            } else {
                url
            }
            ImageRequest.Builder(context)
                .data(imageData)
                .crossfade(true)
                .setHeader("User-Agent", "EcoRecycle/1.0 Android")
                .build()
        }
    }
}

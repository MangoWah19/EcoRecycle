package com.example.fyp1.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

private val EcoImageGradient = Brush.linearGradient(listOf(Color(0xFF245F35), Color(0xFF008A95)))
private val EcoImagePrimary = Color(0xFF007F2A)

@Composable
internal fun EcoLoadingImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector = Icons.Default.Eco,
    loadingText: String = "Loading image..."
) {
    if (model == null) {
        EcoImageFallback(modifier = modifier, icon = fallbackIcon)
        return
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            EcoImageLoadingPlaceholder(
                modifier = Modifier.fillMaxSize(),
                loadingText = loadingText
            )
        },
        error = {
            EcoImageFallback(
                modifier = Modifier.fillMaxSize(),
                icon = fallbackIcon
            )
        }
    )
}

@Composable
private fun EcoImageLoadingPlaceholder(modifier: Modifier, loadingText: String) {
    Box(
        modifier = modifier.background(Color(0xFFEFF5F0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = EcoImagePrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = loadingText,
                color = EcoImagePrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EcoImageFallback(modifier: Modifier, icon: ImageVector) {
    Box(
        modifier = modifier.background(EcoImageGradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.34f),
            modifier = Modifier.size(86.dp)
        )
    }
}

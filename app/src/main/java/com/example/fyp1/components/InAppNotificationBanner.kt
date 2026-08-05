package com.example.fyp1.components

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fyp1.offline.LocalNotificationEntity

@Composable
fun InAppNotificationBanner(
    notification: LocalNotificationEntity?,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var renderedNotification by remember { mutableStateOf<LocalNotificationEntity?>(null) }

    LaunchedEffect(notification) {
        if (notification != null) {
            renderedNotification = notification
            visible = true
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible && renderedNotification != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier
    ) {
        val currentNotification = renderedNotification ?: return@AnimatedVisibility

        val accent = currentNotification.bannerAccent()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 54.dp, bottom = 18.dp)
                .shadow(18.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentNotification.bannerIcon(),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = currentNotification.title,
                        color = Color(0xFF1D1F1D),
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentNotification.message,
                        color = Color(0xFF5D6460),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun LocalNotificationEntity.bannerAccent(): Color =
    when (category) {
        "MISSION" -> Color(0xFF007A3D)
        "RECYCLING" -> Color(0xFF16A34A)
        "REWARD" -> Color(0xFF008B56)
        "LEADERBOARD" -> Color(0xFF00A7B8)
        else -> Color(0xFF006B1B)
    }

private fun LocalNotificationEntity.bannerIcon(): ImageVector =
    when (category) {
        "MISSION" -> Icons.Default.EmojiEvents
        "RECYCLING" -> Icons.Default.Recycling
        "REWARD" -> Icons.Default.Redeem
        "LEADERBOARD" -> Icons.Default.Stars
        else -> Icons.Default.CheckCircle
    }

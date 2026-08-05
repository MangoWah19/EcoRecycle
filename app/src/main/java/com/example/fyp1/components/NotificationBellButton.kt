package com.example.fyp1.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fyp1.api.NotificationRepository

@Composable
fun NotificationBellButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { NotificationRepository(context) }
    val unreadCount by repository.observeUnreadCount().collectAsState(initial = 0)

    Box(modifier = modifier.size(42.dp)) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = CircleShape,
            color = Color(0xFFE6E9E7),
            border = BorderStroke(2.dp, Color(0x1A006B1B))
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF006B1B),
                modifier = Modifier.padding(9.dp)
            )
        }

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (unreadCount > 9) 22.dp else 18.dp)
                    .background(Color(0xFFB02500), CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = if (unreadCount > 9) 8.sp else 9.sp,
                    lineHeight = if (unreadCount > 9) 8.sp else 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

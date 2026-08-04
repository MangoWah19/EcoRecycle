package com.example.fyp1.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class PopOutMessageType {
    Error,
    Success,
    Info
}

data class AppPopOutMessage(
    val title: String,
    val message: String,
    val type: PopOutMessageType = PopOutMessageType.Error,
    val buttonText: String = "Got it",
    val secondaryButtonText: String? = null
)

@Composable
fun AppPopOutDialog(
    message: AppPopOutMessage?,
    onDismiss: () -> Unit,
    onPrimary: (() -> Unit)? = null,
    onSecondary: (() -> Unit)? = null
) {
    if (message == null) return

    val accent = when (message.type) {
        PopOutMessageType.Error -> Color(0xFFFF5A5F)
        PopOutMessageType.Success -> Color(0xFF43A047)
        PopOutMessageType.Info -> Color(0xFF00796B)
    }
    val icon: ImageVector = when (message.type) {
        PopOutMessageType.Error -> Icons.Default.ErrorOutline
        PopOutMessageType.Success -> Icons.Default.CheckCircle
        PopOutMessageType.Info -> Icons.Default.Info
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .shadow(18.dp, RoundedCornerShape(34.dp)),
            shape = RoundedCornerShape(34.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = message.title,
                    color = Color(0xFF1D1F1D),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message.message,
                    color = Color(0xFF747776),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                if (message.secondaryButtonText != null && onSecondary != null) {
                    Button(
                        onClick = onPrimary ?: onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text(
                            text = message.buttonText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7F2EA))
                    ) {
                        Text(
                            text = message.secondaryButtonText,
                            color = Color(0xFF006B1B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Button(
                        onClick = onPrimary ?: onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text(
                            text = message.buttonText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

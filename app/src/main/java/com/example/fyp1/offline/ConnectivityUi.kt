package com.example.fyp1.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ConnectionUiMode {
    Online,
    Offline,
    Syncing
}

@Composable
fun rememberConnectionUiMode(): ConnectionUiMode {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(context.isNetworkAvailable()) }
    var mode by remember { mutableStateOf(if (isOnline) ConnectionUiMode.Online else ConnectionUiMode.Offline) }
    var hasBeenOffline by remember { mutableStateOf(!isOnline) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mainHandler = Handler(Looper.getMainLooper())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainHandler.post { isOnline = true }
            }

            override fun onLost(network: Network) {
                mainHandler.post { isOnline = context.isNetworkAvailable() }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                mainHandler.post { isOnline = context.isNetworkAvailable() }
            }
        }

        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
        onDispose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            hasBeenOffline = true
            mode = ConnectionUiMode.Offline
            return@LaunchedEffect
        }

        if (hasBeenOffline) {
            mode = ConnectionUiMode.Syncing
            OfflineWorkManager.enqueuePendingMissionUploads(context)
            OfflineWorkManager.enqueueSync(context)
            delay(1500)
            mode = if (context.isNetworkAvailable()) ConnectionUiMode.Online else ConnectionUiMode.Offline
            hasBeenOffline = mode == ConnectionUiMode.Offline
        } else {
            mode = ConnectionUiMode.Online
        }
    }

    return mode
}

@Composable
fun ConnectionModeChip(mode: ConnectionUiMode, modifier: Modifier = Modifier, showOnline: Boolean = false) {
    if (mode == ConnectionUiMode.Online && !showOnline) return

    val label = when (mode) {
        ConnectionUiMode.Online -> "ONLINE MODE"
        ConnectionUiMode.Offline -> "OFFLINE MODE"
        ConnectionUiMode.Syncing -> "SYNCING..."
    }
    val tint = when (mode) {
        ConnectionUiMode.Online -> Color(0xFF006B1B)
        ConnectionUiMode.Offline -> Color(0xFF4E9F5D)
        ConnectionUiMode.Syncing -> Color(0xFF006B1B)
    }
    val background = when (mode) {
        ConnectionUiMode.Online -> Color(0xFFE2F6E6)
        ConnectionUiMode.Offline -> Color.Transparent
        ConnectionUiMode.Syncing -> Color(0xFFEAF4ED)
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = background,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode == ConnectionUiMode.Syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = tint,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = label,
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OfflineCapabilityNotice(mode: ConnectionUiMode, modifier: Modifier = Modifier) {
    val message = when (mode) {
        ConnectionUiMode.Offline -> "Offline mode: cached missions and learning content are available. Mission proofs will be saved as Pending Upload and synced when internet returns. Login, quizzes, rewards, and fresh status updates need internet."
        ConnectionUiMode.Syncing -> "Syncing saved uploads and refreshing your latest data..."
        ConnectionUiMode.Online -> null
    } ?: return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (mode == ConnectionUiMode.Offline) Color(0xFFF0F6F1) else Color(0xFFE2F6E6),
        border = BorderStroke(1.dp, Color(0x33006B1B))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = Color(0xFF4F5A53),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

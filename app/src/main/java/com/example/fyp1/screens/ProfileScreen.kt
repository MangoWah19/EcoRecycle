package com.example.fyp1.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.components.AppPopOutDialog
import com.example.fyp1.components.AppPopOutMessage
import com.example.fyp1.components.EcoNavigationDrawer
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.components.NotificationBellButton
import com.example.fyp1.components.PopOutMessageType
import com.example.fyp1.offline.ConnectionModeChip
import com.example.fyp1.offline.ConnectionUiMode
import com.example.fyp1.offline.rememberConnectionUiMode
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val connectionMode = rememberConnectionUiMode()
    val authRepository = remember { AuthRepository(context) }
    var popOutMessage by remember { mutableStateOf<AppPopOutMessage?>(null) }
    val recycledKg = viewModel.recyclingHistory.sumOf { it.quantity }
    val approvedDeposits = viewModel.recyclingHistory.count { it.status.equals("Approved", ignoreCase = true) }

    LaunchedEffect(Unit) {
        viewModel.refreshHomeProfileData(context)
    }

    EcoNavigationDrawer(navController = navController) { openDrawer ->
    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileTopBar(
                    connectionMode = connectionMode,
                    onMenuClick = openDrawer,
                    onNotificationsClick = { navController.navigate("notifications") }
                )
            }

            item {
                ProfileIdentity(
                    userName = viewModel.userName.ifBlank { "Eco Student" },
                    isOffline = connectionMode == ConnectionUiMode.Offline,
                    onEdit = { navController.navigate("edit_profile") }
                )
            }

            item {
                ProfileStatsCard(
                    recycledKg = recycledKg,
                    savedCount = approvedDeposits,
                    points = viewModel.userPoints
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileActionButton(
                        title = "Recycling Guide",
                        icon = Icons.Default.Recycling,
                        backgroundColor = Color(0xFFEFF8F0),
                        contentColor = Color(0xFF006B1B),
                        iconBackgroundColor = Color(0x1A006B1B),
                        onClick = { navController.navigate("recycling_guide") }
                    )
                    ProfileActionButton(
                        title = "My Recycling",
                        icon = Icons.Default.History,
                        backgroundColor = Color(0xFFF0FFF4),
                        contentColor = Color(0xFF006A38),
                        iconBackgroundColor = Color(0x1A006A38),
                        onClick = { navController.navigate("recycling_history") }
                    )
                    ProfileActionButton(
                        title = "Point Transaction",
                        icon = Icons.Default.ReceiptLong,
                        backgroundColor = Color(0xFFFFF8E7),
                        contentColor = Color(0xFF8A6500),
                        iconBackgroundColor = Color(0x1A8A6500),
                        onClick = { navController.navigate("point_transactions") }
                    )
                    ProfileActionButton(
                        title = "Saved Content",
                        icon = Icons.Default.Bookmark,
                        backgroundColor = Color(0xFFF0FBFF),
                        contentColor = Color(0xFF00656F),
                        iconBackgroundColor = Color(0x1A00656F),
                        onClick = { navController.navigate("saved_content") }
                    )
                }
            }

            item {
                SignOutOutlineButton(
                    onClick = {
                        scope.launch {
                            try {
                                authRepository.clearSession()
                                viewModel.clearBackendUser()
                                navController.navigate("login") { popUpTo(0) }
                            } catch (e: Exception) {
                                popOutMessage = AppPopOutMessage(
                                    title = "Sign Out Failed",
                                    message = "We could not sign you out right now. Please try again.",
                                    type = PopOutMessageType.Error
                                )
                            }
                        }
                    }
                )
            }
        }
    }
    }

    AppPopOutDialog(
        message = popOutMessage,
        onDismiss = { popOutMessage = null }
    )
}

@Composable
private fun ProfileTopBar(
    connectionMode: ConnectionUiMode,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF006B1B)
                )
            }
            Text(
                text = "Profile",
                color = Color(0xFF006B1B),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionModeChip(connectionMode)
            NotificationBellButton(onClick = onNotificationsClick)
        }
    }
}

@Composable
private fun ProfileIdentity(userName: String, isOffline: Boolean, onEdit: () -> Unit) {
    val editModifier = if (isOffline) {
        Modifier
            .size(36.dp)
            .clip(CircleShape)
    } else {
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onEdit
            )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFFEAF0EC),
                border = BorderStroke(4.dp, Color.White),
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF006B1B),
                    modifier = Modifier.padding(22.dp)
                )
            }
            Surface(
                modifier = editModifier,
                shape = CircleShape,
                color = if (isOffline) Color(0xFFB8C0BC) else Color(0xFF00751D),
                border = BorderStroke(3.dp, Color.White),
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isOffline) "Edit profile unavailable offline" else "Edit profile",
                    tint = Color.White,
                    modifier = Modifier.padding(9.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = userName,
            color = Color(0xFF2C2F2E),
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileStatsCard(recycledKg: Double, savedCount: Int, points: Int) {
    val recycledText = if (recycledKg > 0.0) {
        if (recycledKg % 1.0 == 0.0) "${recycledKg.toInt()}kg" else "%.1fkg".format(recycledKg)
    } else {
        "--kg"
    }
    val savedText = if (savedCount > 0) savedCount.toString() else "--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E9E7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(value = recycledText, label = "RECYCLED")
            StatDivider()
            ProfileStat(value = savedText, label = "APPROVED")
            StatDivider()
            ProfileStat(value = points.toString(), label = "POINTS")
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color(0xFF00751D),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color(0x99595C5B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(Color(0x1A595C5B))
    )
}

@Composable
private fun ProfileActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    iconBackgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = iconBackgroundColor
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.size(16.dp))
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SignOutOutlineButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0x40B02500))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color(0xFFB02500),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Sign Out",
                color = Color(0xFFB02500),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun formatKg(value: Double): String {
    return if (value % 1.0 == 0.0) {
        "${value.toInt()}kg"
    } else {
        "${String.format("%.1f", value)}kg"
    }
}












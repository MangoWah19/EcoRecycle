package com.example.fyp1.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recycledKg = viewModel.recyclingHistory.sumOf { it.quantity }
    val approvedDeposits = viewModel.recyclingHistory.count { it.status.equals("Approved", ignoreCase = true) }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { ProfileTopBar() }

            item {
                ProfileIdentity(
                    userName = viewModel.userName.ifBlank { "Eco Student" },
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
                        backgroundColor = Color(0xFF00751D),
                        contentColor = Color.White,
                        iconBackgroundColor = Color.White.copy(alpha = 0.20f),
                        onClick = { navController.navigate("recycling_guide") }
                    )
                    ProfileActionButton(
                        title = "My Recycling",
                        icon = Icons.Default.History,
                        backgroundColor = Color(0xFF86FAAC),
                        contentColor = Color(0xFF004A25),
                        iconBackgroundColor = Color(0x1A004A25),
                        onClick = { navController.navigate("recycling_history") }
                    )
                    ProfileActionButton(
                        title = "Saved Content",
                        icon = Icons.Default.Bookmark,
                        backgroundColor = Color(0xFF11EAFE),
                        contentColor = Color(0xFF003D43),
                        iconBackgroundColor = Color(0x1A003D43),
                        onClick = {
                            Toast.makeText(context, "Saved Content coming soon", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                SignOutOutlineButton(
                    onClick = {
                        scope.launch {
                            try {
                                supabase.auth.signOut()
                                navController.navigate("login") { popUpTo(0) }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Sign Out Failed: Unable to sign out. Please check your connection and try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Profile",
            color = Color(0xFF006B1B),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ProfileIdentity(userName: String, onEdit: () -> Unit) {
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
                shadowElevation = 7.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF006B1B),
                    modifier = Modifier.padding(22.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEdit),
                shape = CircleShape,
                color = Color(0xFF00751D),
                border = BorderStroke(3.dp, Color.White),
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E6E2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 17.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(value = "Live", label = "RECYCLED", helper = formatKg(recycledKg))
            StatDivider()
            ProfileStat(value = "Live", label = "SAVED", helper = savedCount.toString())
            StatDivider()
            ProfileStat(value = "Live", label = "POINTS", helper = points.toString())
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, helper: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color(0xFF00751D),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color(0x99595C5B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = helper,
            color = Color(0xFF595C5B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
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
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 7.dp
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
        shape = CircleShape,
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
package com.example.fyp1.components

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.fyp1.ui.theme.FYP1Theme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.fyp1.*

@Composable
fun AchievementCard(achievement: AchievementBadge) {
    val progress = if (achievement.target > 0)
        (achievement.current / achievement.target).toFloat().coerceIn(0f, 1f)
    else 1f

    val remaining = (achievement.target - achievement.current).coerceAtLeast(0.0)

    val accentColor = when (achievement.type) {
        "plastic_king"      -> Color(0xFF2196F3)
        "paper_master"      -> Color(0xFF4CAF50)
        "glass_guard"       -> Color(0xFF009688)
        "eco_warrior"       -> Color(0xFF1DB954)
        "week_streak"       -> Color(0xFFFF5722)
        "first_redemption"  -> Color(0xFF9C27B0)
        "reward_collector"  -> Color(0xFFFF9800)
        else                -> Color(0xFF1DB954)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) accentColor.copy(alpha = 0.08f) else Color.White
        ),
        border = BorderStroke(
            1.5.dp,
            if (achievement.isUnlocked) accentColor else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: icon + title + trophy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (achievement.isUnlocked) accentColor.copy(alpha = 0.15f)
                            else Color(0xFFF5F5F5),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = achievement.icon, fontSize = 26.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (achievement.isUnlocked) accentColor else Color(0xFF333333)
                    )
                    Text(
                        text = achievement.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (achievement.isUnlocked) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (achievement.isUnlocked) accentColor else accentColor.copy(alpha = 0.5f),
                trackColor = Color(0xFFEEEEEE)
            )

            Spacer(Modifier.height(8.dp))

            // Progress label row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (achievement.isUnlocked) {
                    Text(
                        text = "Completed!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                } else {
                    val currentDisplay = if (achievement.current == achievement.current.toLong().toDouble())
                        achievement.current.toLong().toString()
                    else String.format("%.1f", achievement.current)

                    val remainingDisplay = if (remaining == remaining.toLong().toDouble())
                        remaining.toLong().toString()
                    else String.format("%.1f", remaining)

                    Text(
                        text = "$currentDisplay / ${achievement.target.toLong()} ${achievement.unit}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$remainingDisplay ${achievement.unit} left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) accentColor else Color.Gray
                )
            }
        }
    }
}


@Composable
fun LeaderboardRowWithRankChange(entry: LeaderboardEntryWithRank, timeframe: String) {
    val rank = entry.rank
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF1DB954)
    }
    val rankIcon = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$rank"
    }
    val rankChangeColor = when (entry.rank_change) {
        "↑" -> Color(0xFF2E7D32)
        "↓" -> Color(0xFFC62828)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) rankColor.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank <= 3) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (rank <= 3) rankColor.copy(alpha = 0.2f) else Color(0xFFE8F5E9),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Text(rankIcon, fontSize = 24.sp)
                } else {
                    Text(
                        "$rank",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Surface(
                shape = CircleShape,
                color = Color(0xFF1DB954).copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.full_name ?: "Student",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = when (timeframe) {
                        "daily"  -> "Today: ${entry.total_points} pts"
                        "weekly" -> "This week: ${entry.total_points} pts"
                        else     -> ""
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when (timeframe) {
                        "daily", "weekly" -> "${entry.total_points}"
                        else -> "${entry.lifetime_points}"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (rank <= 3) rankColor else Color(0xFF1DB954)
                )
                Text(
                    text = "pts",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                // Rank change indicator (闂?闂?闂?
                entry.rank_change?.let { change ->
                    Text(
                        text = change,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankChangeColor
                    )
                }
            }
        }
    }
}

// Keep the original LeaderboardRow for backward compatibility
@Composable
fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF1DB954)
    }
    val rankIcon = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "$rank" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (rank <= 3) rankColor.copy(alpha = 0.1f) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank <= 3) 4.dp else 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(if (rank <= 3) rankColor.copy(alpha = 0.2f) else Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                if (rank <= 3) Text(rankIcon, fontSize = 24.sp)
                else Text("$rank", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
            }
            Spacer(Modifier.width(16.dp))
            Surface(shape = CircleShape, color = Color(0xFF1DB954).copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF1DB954), modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.full_name ?: "Student", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Current Balance: ${entry.total_points} pts", fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.lifetime_points}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = if (rank <= 3) rankColor else Color(0xFF1DB954))
                Text("pts earned", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun RedemptionHistoryRow(redemption: Redemption) {
    val (stateLabel, stateColor) = when (redemption.status) {
        "claimed"   -> Pair("CLAIMED",   Color(0xFFEF6C00))
        "redeemed"  -> Pair("REDEEMED",  Color(0xFF2E7D32))
        "expired"   -> Pair("EXPIRED",   Color(0xFFC62828))
        "completed" -> Pair("COMPLETED", Color(0xFF2E7D32))
        else        -> Pair(redemption.status.uppercase(), Color.Gray)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(redemption.item_name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(redemption.created_at?.take(10) ?: "Unknown date", fontSize = 10.sp, color = Color.Gray)
                redemption.expires_at?.let {
                    Text("Expires: ${it.take(10)}", fontSize = 10.sp, color = Color(0xFFEF6C00))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-${redemption.points_spent} pts", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = stateColor.copy(alpha = 0.1f)) {
                    Text(
                        stateLabel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }
            }
        }
    }
}

@Composable
fun RewardCard(reward: Reward, onRedeem: (Int) -> Unit) {
    var quantity by remember { mutableIntStateOf(1) }
    var showFullImage by remember { mutableStateOf(false) }

    if (showFullImage) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showFullImage = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = reward.image_url?.trim()
                        ?.takeIf { it.isNotBlank() && (it.startsWith("https://")) }
                        ?: "https://placehold.co/400x200/e8f5e9/1db954?text=No+Image",
                    contentDescription = reward.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFE8F5E9))
                        .clickable { showFullImage = true },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Text(
                    text = "Tap anywhere to close",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }

    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column {
            AsyncImage(
                model = reward.image_url?.takeIf { it.isNotBlank() }
                    ?: "https://placehold.co/400x200/e8f5e9/1db954?text=No+Image",
                contentDescription = reward.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFE8F5E9))
                    .clickable { showFullImage = true },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )
            Column(Modifier.padding(16.dp)) {
                Text(reward.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${reward.points_required} pts each", color = Color(0xFF1DB954), fontWeight = FontWeight.Bold)
                Text("Stock: ${reward.stock}", fontSize = 12.sp, color = if (reward.stock > 0) Color.Gray else Color.Red)

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1DB954))
                        }

                        Text(
                            text = "$quantity",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { if (quantity < reward.stock) quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1DB954))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "${reward.points_required * quantity} pts",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onRedeem(quantity) },
                    enabled = reward.stock > 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text(if (reward.stock > 0) "Redeem $quantity item${if (quantity > 1) "s" else ""}" else "Sold Out")
                }
            }
        }
    }
}


@Composable
fun LogHistoryRow(log: RecyclingLog) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFFE8F5E9), modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.History, null, tint = Color(0xFF1B5E20), modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.material_type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    val displayTime = log.created_at?.replace("T", " ")?.take(16) ?: "Just now"
                    Text(text = displayTime, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (log.status) {
                    "Approved" -> Color(0xFF2E7D32)
                    "Rejected" -> Color(0xFFC62828)
                    else -> Color(0xFFEF6C00)
                }
                Text(
                    text = if (log.status == "Approved") "+${log.points_awarded} pts" else "Pending",
                    color = statusColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                )
                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(
                        text = log.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor
                    )
                }
            }
        }
    }
}


@Composable
fun GuideCard(guide: MaterialGuide, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(guide.icon, null, tint = guide.color)
            Spacer(Modifier.width(16.dp))
            Text(guide.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(guide.points, fontSize = 12.sp)
        }
    }
}


@Composable
fun HistoryRow(redemption: Redemption) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween) {
        Column {
            Text(redemption.item_name, fontWeight = FontWeight.Medium)
            Text(redemption.created_at?.take(10) ?: "", fontSize = 10.sp, color = Color.Gray)
        }
        Text("-${redemption.points_spent} pts", color = Color.Red, fontWeight = FontWeight.Bold)
    }
}

// ============================================
// UTILITY FUNCTIONS

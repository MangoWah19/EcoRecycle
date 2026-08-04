package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendPointsEvent
import com.example.fyp1.api.PointsData
import com.example.fyp1.api.PointsRepository
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.offline.OfflineWorkManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LedgerBackground = Color(0xFFF5F7F5)
private val LedgerPrimary = Color(0xFF006B1B)
private val LedgerText = Color(0xFF2C2F2E)
private val LedgerMuted = Color(0xFF66706B)

private enum class LedgerTab(val label: String) {
    All("ALL"),
    Earned("EARNED"),
    Spent("SPENT")
}

@Composable
fun PointLedgerScreen(navController: NavController) {
    val context = LocalContext.current
    val pointsRepository = remember { PointsRepository(context) }
    val tabs = listOf(LedgerTab.All, LedgerTab.Earned, LedgerTab.Spent)
    var selectedTab by remember { mutableIntStateOf(0) }
    var pointsData by remember { mutableStateOf(PointsData()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        OfflineWorkManager.enqueueSync(context)
        when (val result = pointsRepository.getMyPoints()) {
            is AuthResult.Success -> pointsData = result.value
            is AuthResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    val filteredEvents = pointsData.events.filter { event ->
        when (tabs[selectedTab]) {
            LedgerTab.All -> true
            LedgerTab.Earned -> event.isEarned()
            LedgerTab.Spent -> event.isSpent()
        }
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LedgerBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { LedgerTopBar(onBack = { navController.popBackStack() }) }
            item {
                LedgerBalanceCard(
                    currentBalance = pointsData.total,
                    lifetimeTotal = pointsData.lifetimeTotal
                )
            }
            item {
                LedgerTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }
            item {
                Text(
                    text = "Recent Activity",
                    color = LedgerText.copy(alpha = 0.82f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            when {
                isLoading -> {
                    item { LedgerInfoCard("Loading point transactions...", loading = true) }
                }
                errorMessage != null -> {
                    item { LedgerInfoCard(errorMessage.orEmpty()) }
                }
                filteredEvents.isEmpty() -> {
                    item { LedgerInfoCard("No ${tabs[selectedTab].label.lowercase()} point transactions yet.") }
                }
                else -> {
                    items(filteredEvents, key = { it.id }) { event ->
                        LedgerEventCard(event)
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LedgerTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LedgerPrimary)
        }
        Text(
            text = "Point Transactions",
            color = LedgerPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun LedgerBalanceCard(currentBalance: Int, lifetimeTotal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CURRENT BALANCE", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("%,d".format(currentBalance), color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.ExtraBold)
                    Text(" pts", color = Color.White.copy(alpha = 0.80f), fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 9.dp))
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("LIFETIME EARNINGS", color = Color.White.copy(alpha = 0.62f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                        Text("%,d pts".format(lifetimeTotal), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.78f), modifier = Modifier.padding(7.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerTabs(tabs: List<LedgerTab>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = CircleShape, color = Color(0xFFE6E9E7)) {
        Row(modifier = Modifier.padding(5.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clickable { onSelected(index) },
                    shape = CircleShape,
                    color = if (selected) LedgerPrimary else Color.Transparent,
                    shadowElevation = if (selected) 4.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tab.label,
                            color = if (selected) Color.White else Color(0xFF595C5B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerEventCard(event: BackendPointsEvent) {
    val style = event.ledgerStyle()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8EBE9))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = style.color.copy(alpha = 0.14f)) {
                Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.padding(11.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = style.color.copy(alpha = 0.12f)) {
                        Text(
                            text = style.label.uppercase(),
                            color = style.color,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                    Text(formatLedgerDate(event.approvedAt ?: event.createdAt), color = LedgerMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = style.title,
                    color = LedgerText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.ledgerReference(),
                    color = LedgerMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = event.pointsText(),
                    color = style.amountColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(event.status.uppercase(), color = style.amountColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun LedgerInfoCard(message: String, loading: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(26.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator(color = LedgerPrimary, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                Spacer(Modifier.height(12.dp))
            }
            Text(message, color = LedgerMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private data class LedgerEventStyle(
    val label: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val amountColor: Color
)

private fun BackendPointsEvent.ledgerStyle(): LedgerEventStyle {
    val type = eventType.uppercase()
    return when {
        type.contains("REWARD") || isSpent() -> LedgerEventStyle("Reward Redeemed", "Reward redemption", Icons.Default.Redeem, Color(0xFFB02500), Color(0xFFB02500))
        type.contains("MISSION") -> LedgerEventStyle("Mission Completed", "Mission points", Icons.Default.Stars, Color(0xFF00656F), LedgerPrimary)
        type.contains("RECYCLING") -> LedgerEventStyle("Recycling Approved", "Recycling deposit", Icons.Default.Recycling, LedgerPrimary, LedgerPrimary)
        else -> LedgerEventStyle(eventType.replace('_', ' '), "Point adjustment", Icons.Default.ReceiptLong, Color(0xFF5F6662), if (points >= 0) LedgerPrimary else Color(0xFFB02500))
    }
}

private fun BackendPointsEvent.isSpent(): Boolean {
    return points < 0 || eventType.equals("REWARD_REDEEMED", ignoreCase = true)
}

private fun BackendPointsEvent.isEarned(): Boolean {
    return !isSpent() && points > 0
}

private fun BackendPointsEvent.pointsText(): String {
    return if (points >= 0) "+$points" else points.toString()
}

private fun BackendPointsEvent.ledgerReference(): String {
    return when {
        !recyclingSubmissionId.isNullOrBlank() -> "Recycling submission: $recyclingSubmissionId"
        !missionId.isNullOrBlank() -> "Mission: $missionId"
        !submissionId.isNullOrBlank() -> "Mission submission: $submissionId"
        !redemptionId.isNullOrBlank() -> "Reward redemption: $redemptionId"
        else -> "Point event: $id"
    }
}

private fun formatLedgerDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "Date unavailable"
    return runCatching {
        val instant = Instant.parse(raw)
        DateTimeFormatter.ofPattern("MMM d, h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrElse { raw.take(16) }
}

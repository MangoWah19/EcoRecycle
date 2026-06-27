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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fyp1.MainViewModel
import com.example.fyp1.Redemption
import com.example.fyp1.Reward
import com.example.fyp1.components.FloatingBottomNavigationScaffold

private enum class RewardTab(val label: String) {
    Browse("BROWSE REWARDS"),
    MyRewards("MY REWARDS"),
    History("MY HISTORY")
}

@Composable
fun RewardsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(RewardTab.Browse, RewardTab.MyRewards, RewardTab.History)
    val activeRedemptions = viewModel.redemptionHistory.filter { it.status.equals("claimed", ignoreCase = true) }

    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(padding)
                .padding(bottom = 110.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                RewardsHeader(onBack = { navController.popBackStack() })
            }

            item {
                RewardBalanceCard(
                    points = viewModel.userPoints,
                    lifetimePoints = viewModel.lifetimePoints
                )
            }

            item {
                RewardTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }

            when (tabs[selectedTab]) {
                RewardTab.History -> {
                    item {
                        RewardSectionHeader(
                            title = "Redemption History",
                            meta = "RECENT FIRST"
                        )
                    }
                    if (viewModel.redemptionHistory.isEmpty()) {
                        item { EmptyRewardState("No redemption history yet") }
                    } else {
                        items(viewModel.redemptionHistory) { redemption ->
                            RedemptionHistoryCard(
                                redemption = redemption,
                                reward = findRewardForRedemption(redemption, viewModel.rewardsCatalog)
                            )
                        }
                        item { EndOfHistoryLabel() }
                    }
                }

                RewardTab.MyRewards -> {
                    item {
                        RewardSectionHeader(
                            title = "My Active Rewards",
                            meta = "${activeRedemptions.size} ITEMS"
                        )
                    }
                    if (activeRedemptions.isEmpty()) {
                        item { EmptyRewardState("No claimed rewards yet") }
                    } else {
                        items(activeRedemptions) { redemption ->
                            ClaimedRewardCard(
                                redemption = redemption,
                                reward = findRewardForRedemption(redemption, viewModel.rewardsCatalog)
                            )
                        }
                    }
                }

                RewardTab.Browse -> {
                    if (viewModel.rewardsCatalog.isEmpty()) {
                        item { EmptyRewardState("No rewards available right now") }
                    } else {
                        items(viewModel.rewardsCatalog) { reward ->
                            BrowseRewardCard(
                                reward = reward,
                                userPoints = viewModel.userPoints,
                                onRedeem = { quantity -> viewModel.redeemItem(reward, quantity, context) }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun RewardsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF006B1B)
            )
        }
        Text(
            text = "Redeem Reward",
            color = Color(0xFF006B1B),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun RewardBalanceCard(points: Int, lifetimePoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00751D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "AVAILABLE BALANCE",
                color = Color(0xCCD1FFC8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.6.sp
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = points.toString(),
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = " PTS",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = "LIFETIME: ${"%,d".format(lifetimePoints)} PTS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardTabs(
    tabs: List<RewardTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = Color(0xFFEFF1EF)
    ) {
        Row(modifier = Modifier.padding(5.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = selectedIndex == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { onSelected(index) },
                    shape = CircleShape,
                    color = if (selected) Color.White else Color.Transparent,
                    shadowElevation = if (selected) 3.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tab.label,
                            color = if (selected) Color(0xFF006B1B) else Color(0xFF595C5B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardSectionHeader(title: String, meta: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            color = Color(0xFF006B1B),
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = meta,
            color = Color(0xFF595C5B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun BrowseRewardCard(
    reward: Reward,
    userPoints: Int,
    onRedeem: (Int) -> Unit
) {
    var quantity by remember(reward.id) { mutableIntStateOf(1) }
    val totalCost = reward.points_required * quantity
    val canRedeem = reward.stock > 0 && userPoints >= totalCost

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E9E7))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.name,
                        color = Color(0xFF2C2F2E),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${reward.points_required} pts each",
                        color = Color(0xFF006B1B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Stock: ${reward.stock}",
                    color = Color(0xFF595C5B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E9E7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = reward.image_url?.takeIf { it.isNotBlank() },
                        contentDescription = reward.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.94f)
                    ) {
                        Text(
                            text = "VERIFIED MERCHANT",
                            color = Color(0xFF006B1B),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFEFF1EF), CircleShape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF2C2F2E))
                    }
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (quantity < reward.stock.coerceAtLeast(1)) quantity++ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF86FAAC), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF005F32))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL COST",
                        color = Color(0xFF595C5B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = totalCost.toString(),
                            color = Color(0xFF006B1B),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " PTS",
                            color = Color(0xFF006B1B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    if (canRedeem) onRedeem(quantity)
                },
                enabled = canRedeem,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B1B))
            ) {
                Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Redeem  $quantity item${if (quantity > 1) "s" else ""}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
@Composable
private fun RedemptionHistoryCard(redemption: Redemption, reward: Reward?) {
    RewardListCard(
        title = redemption.item_name,
        imageUrl = reward?.image_url,
        subtitle = formatDateTime(redemption.created_at ?: redemption.claimed_at),
        status = redemption.status,
        pointsText = "-${redemption.points_spent} pts",
        statusColor = redemptionStatusColor(redemption.status),
        trailingTextColor = Color(0xFFB02500),
        expiryText = null
    )
}

@Composable
private fun ClaimedRewardCard(redemption: Redemption, reward: Reward?) {
    val expiry = redemption.expires_at?.let { "EXPIRES: ${formatDateOnly(it)}" }
        ?: "EXPIRES: TODO"
    // TODO: Use backend-provided expiry display once redemption expiry format is finalized.
    RewardListCard(
        title = redemption.item_name,
        imageUrl = reward?.image_url,
        subtitle = expiry,
        status = "CLAIMED",
        pointsText = null,
        statusColor = Color(0xFF006B1B),
        trailingTextColor = Color(0xFF595C5B),
        expiryText = expiry
    )
}

@Composable
private fun RewardListCard(
    title: String,
    imageUrl: String?,
    subtitle: String,
    status: String,
    pointsText: String?,
    statusColor: Color,
    trailingTextColor: Color,
    expiryText: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0x0DABAEAC))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE6E9E7)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF006B1B))
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = Color(0xFF2C2F2E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            color = if (expiryText != null) Color(0xFFB02500) else Color(0xFF595C5B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    pointsText?.let {
                        Text(
                            text = it,
                            color = trailingTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = statusColor.copy(alpha = 0.16f)) {
                        Text(
                            text = status.uppercase(),
                            color = statusColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF595C5B), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyRewardState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFF595C5B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(28.dp).fillMaxWidth()
        )
    }
}

@Composable
private fun EndOfHistoryLabel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(3.dp)
                .background(Color(0xFFDADFDB), CircleShape)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "END OF RECENT HISTORY",
            color = Color(0xFF9B9D9C),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

private fun findRewardForRedemption(redemption: Redemption, rewards: List<Reward>): Reward? {
    return rewards.firstOrNull { it.id == redemption.reward_id }
        ?: rewards.firstOrNull { it.name.equals(redemption.item_name, ignoreCase = true) }
}

private fun redemptionStatusColor(status: String): Color = when (status.lowercase()) {
    "claimed" -> Color(0xFF006B1B)
    "redeemed", "completed" -> Color(0xFF595C5B)
    "expired" -> Color(0xFFB02500)
    else -> Color(0xFF747776)
}

private fun formatDateOnly(value: String?): String {
    return value?.take(10) ?: "TODO"
}

private fun formatDateTime(value: String?): String {
    return value
        ?.replace("T", " ")
        ?.take(16)
        ?: "Unknown date"
}
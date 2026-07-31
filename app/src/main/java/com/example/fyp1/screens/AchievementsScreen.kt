package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendBadgeProgress
import com.example.fyp1.api.BadgeRepository
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import kotlinx.coroutines.launch

private val BadgeBackground = Color(0xFFF5F7F5)
private val BadgePrimary = Color(0xFF006B1B)
private val GoldTier = Color(0xFFFFC400)
private val SilverTier = Color(0xFFBFC4C2)
private val BronzeTier = Color(0xFFB87935)

@Suppress("UNUSED_PARAMETER")
@Composable
fun AchievementsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val badgeRepository = remember { BadgeRepository(context) }
    var earnedBadges by remember { mutableStateOf<List<BackendBadgeProgress>>(emptyList()) }
    var lockedBadges by remember { mutableStateOf<List<BackendBadgeProgress>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadBadges() {
        isLoading = true
        errorMessage = null
        when (val result = badgeRepository.getBadgeProgress()) {
            is AuthResult.Success -> {
                earnedBadges = result.value.earned
                lockedBadges = result.value.locked
            }
            is AuthResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadBadges()
    }

    val badges = (earnedBadges + lockedBadges).sortedWith(
        compareBy<BackendBadgeProgress> { tierOrder(it.tier) }
            .thenBy { criteriaOrder(it.criteriaType) }
            .thenBy { it.criteriaValue }
            .thenBy { it.name }
    )

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BadgeBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { BadgeTopBar(onBack = { navController.popBackStack() }) }

            item {
                BadgeCollectionCard(
                    badges = badges,
                    unlockedCount = earnedBadges.size
                )
            }

            when {
                isLoading -> {
                    item {
                        BadgeInfoCard {
                            CircularProgressIndicator(
                                color = BadgePrimary,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Loading badges...",
                                color = Color(0xFF595C5B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        BadgeInfoCard {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = Color(0xFF595C5B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { scope.launch { loadBadges() } },
                                colors = ButtonDefaults.buttonColors(containerColor = BadgePrimary),
                                shape = CircleShape
                            ) {
                                Text("Retry", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                badges.isEmpty() -> {
                    item {
                        BadgeInfoCard {
                            Text(
                                text = "No badges available yet.",
                                color = Color(0xFF595C5B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                else -> {
                    val groupedBadges = badges.groupBy { it.tier.uppercase() }
                    listOf("GOLD", "SILVER", "BRONZE").forEach { tier ->
                        val tierBadges = groupedBadges[tier].orEmpty()
                        if (tierBadges.isNotEmpty()) {
                            item {
                                BadgeTierHeader(
                                    tier = tier,
                                    count = tierBadges.count { it.isEarned() }
                                )
                            }
                            items(
                                items = tierBadges,
                                key = { it.badgeId }
                            ) { badge ->
                                DynamicBadgeCard(badge)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BadgeTopBar(onBack: () -> Unit) {
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
                tint = BadgePrimary
            )
        }
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = BadgePrimary,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(18.dp)
        )
        Text(
            text = "Badges",
            color = BadgePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun BadgeCollectionCard(
    badges: List<BackendBadgeProgress>,
    unlockedCount: Int
) {
    val totalCount = badges.size
    val progress = if (totalCount == 0) 0f else (unlockedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = BadgePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Badge",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 25.sp
                    )
                    Text(
                        text = "Collection",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 25.sp
                    )
                }
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "MASTERY PROGRESS",
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$unlockedCount / $totalCount Unlocked",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.22f)
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeSummaryPill("TOTAL", totalCount, Modifier.weight(1f))
                BadgeSummaryPill("GOLD", badges.countTier("GOLD"), Modifier.weight(1f))
                BadgeSummaryPill("SILVER", badges.countTier("SILVER"), Modifier.weight(1f))
                BadgeSummaryPill("BRONZE", badges.countTier("BRONZE"), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BadgeSummaryPill(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.64f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun BadgeTierHeader(tier: String, count: Int) {
    val color = tierColor(tier)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "${tierLabel(tier)} Tier",
            color = Color(0xFF2C2F2E),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.14f)
        ) {
            Text(
                text = tierCaption(tier),
                color = tierTextColor(tier),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "$count earned",
            color = Color(0xFF747776),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DynamicBadgeCard(badge: BackendBadgeProgress) {
    val isEarned = badge.isEarned()
    val tierColor = tierColor(badge.tier)
    val progress = (badge.progressPercentage / 100f).coerceIn(0f, 1f)
    val statusLabel = when {
        isEarned -> "UNLOCKED"
        badge.currentProgress > 0 -> "PROGRESSING"
        else -> "LOCKED"
    }
    val cardColor = if (isEarned) Color.White else Color(0xFFFBFCFB)
    val medalColor = if (isEarned || badge.currentProgress > 0) tierColor else Color(0xFFC9CDCA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isEarned) tierColor.copy(alpha = 0.24f) else Color(0xFFE7EBE8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = medalColor,
                    trackColor = Color(0xFFE6E9E7),
                    strokeWidth = 5.dp
                )
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = medalColor
                ) {
                    Icon(
                        imageVector = criteriaIcon(badge.criteriaType),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(15.dp)
                    )
                }
                if (isEarned) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp),
                        shape = CircleShape,
                        color = BadgePrimary
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(3.dp)
                        )
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
                            text = badge.name,
                            color = Color(0xFF2C2F2E),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = badge.description,
                            color = Color(0xFF747776),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!isEarned) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF9AA09C),
                            modifier = Modifier
                                .padding(start = 8.dp, top = 2.dp)
                                .size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isEarned) BadgePrimary.copy(alpha = 0.12f) else Color(0xFFE6E9E7)
                    ) {
                        Text(
                            text = statusLabel,
                            color = if (isEarned) BadgePrimary else Color(0xFF747776),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${badge.currentProgress}/${badge.criteriaValue}",
                        color = Color(0xFF747776),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = criteriaLabel(badge.criteriaType),
                    color = Color(0xFF8A908C),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BadgeInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE7EBE8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

private fun List<BackendBadgeProgress>.countTier(tier: String): Int =
    count { it.tier.equals(tier, ignoreCase = true) }

private fun BackendBadgeProgress.isEarned(): Boolean =
    status.equals("EARNED", ignoreCase = true)

private fun tierOrder(tier: String): Int = when (tier.uppercase()) {
    "GOLD" -> 0
    "SILVER" -> 1
    "BRONZE" -> 2
    else -> 3
}

private fun criteriaOrder(criteriaType: String): Int = when (criteriaType.uppercase()) {
    "MISSIONS_COMPLETED" -> 0
    "APPROVED_SUBMISSIONS" -> 1
    "QUIZZES_PASSED" -> 2
    "CONTENT_COMPLETED" -> 3
    else -> 4
}

private fun tierColor(tier: String): Color = when (tier.uppercase()) {
    "GOLD" -> GoldTier
    "SILVER" -> SilverTier
    "BRONZE" -> BronzeTier
    else -> Color(0xFF86FAAC)
}

private fun tierTextColor(tier: String): Color = when (tier.uppercase()) {
    "GOLD" -> Color(0xFF8A6400)
    "SILVER" -> Color(0xFF5F6662)
    "BRONZE" -> Color(0xFF7A481C)
    else -> BadgePrimary
}

private fun tierLabel(tier: String): String = tier.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun tierCaption(tier: String): String = when (tier.uppercase()) {
    "GOLD" -> "Elite"
    "SILVER" -> "Advanced"
    "BRONZE" -> "Starter"
    else -> "Badge"
}

private fun criteriaLabel(criteriaType: String): String = when (criteriaType.uppercase()) {
    "MISSIONS_COMPLETED" -> "Finished missions"
    "APPROVED_SUBMISSIONS" -> "Approved mission submissions"
    "QUIZZES_PASSED" -> "Passed quiz attempts"
    "CONTENT_COMPLETED" -> "Completed learning content"
    else -> "Badge progress"
}

private fun criteriaIcon(criteriaType: String): ImageVector = when (criteriaType.uppercase()) {
    "MISSIONS_COMPLETED" -> Icons.Default.Stars
    "APPROVED_SUBMISSIONS" -> Icons.Default.Recycling
    "QUIZZES_PASSED" -> Icons.Default.Quiz
    "CONTENT_COMPLETED" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Default.EmojiEvents
}

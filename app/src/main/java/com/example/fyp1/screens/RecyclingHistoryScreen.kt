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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WineBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.RecyclingLog
import com.example.fyp1.components.FloatingBottomNavigationScaffold

private enum class RecyclingHistoryTab(val label: String) {
    All("ALL"),
    Pending("PENDING"),
    Past("PAST")
}

@Composable
fun RecyclingHistoryScreen(navController: NavController, viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAll by remember { mutableStateOf(false) }
    val tabs = listOf(RecyclingHistoryTab.All, RecyclingHistoryTab.Pending, RecyclingHistoryTab.Past)

    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    val logs = viewModel.recyclingHistory
    val filteredLogs = when (tabs[selectedTab]) {
        RecyclingHistoryTab.All -> logs
        RecyclingHistoryTab.Pending -> logs.filter { it.isPendingStatus() }
        RecyclingHistoryTab.Past -> logs.filter { it.isPastStatus() }
    }
    val visibleLogs = if (showAll) filteredLogs else filteredLogs.take(4)
    val totalKg = logs.sumOf { it.quantity }
    val pendingCount = logs.count { it.isPendingStatus() }
    val approvedCount = logs.count { it.status.equals("Approved", ignoreCase = true) }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { RecyclingHistoryTopBar(onBack = { navController.popBackStack() }) }

            item {
                RecyclingImpactCard(totalKg, pendingCount, approvedCount)
            }

            item {
                RecyclingHistoryTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelected = {
                        selectedTab = it
                        showAll = false
                    }
                )
            }

            if (filteredLogs.isEmpty()) {
                item { EmptyHistoryState(tabs[selectedTab]) }
            } else {
                items(visibleLogs) { log ->
                    HistoryLogCard(log = log)
                }
                if (filteredLogs.size > visibleLogs.size) {
                    item { ViewMoreHistoryButton(onClick = { showAll = true }) }
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun RecyclingHistoryTopBar(onBack: () -> Unit) {
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
            text = "Recycling History",
            color = Color(0xFF006B1B),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun RecyclingImpactCard(totalKg: Double, pendingCount: Int, approvedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E9E7))
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("TOTAL IMPACT", color = Color(0xFF595C5B), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatKg(totalKg), color = Color(0xFF006B1B), fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                        Text(" kg", color = Color(0xFF006B1B), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 7.dp))
                    }
                }
                Surface(modifier = Modifier.size(54.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF86FAAC)) {
                    Icon(Icons.Default.Recycling, contentDescription = null, tint = Color(0xFF006B1B), modifier = Modifier.padding(14.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetricCard("PENDING", pendingCount.toString(), Icons.Default.HourglassEmpty, Color(0xFF006B1B), Color.White, Modifier.weight(1f))
                SummaryMetricCard("APPROVED", approvedCount.toString(), Icons.Default.TaskAlt, Color(0xFFE6E9E7), Color(0xFF2C2F2E), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(value, color = content, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(label, color = content.copy(alpha = 0.72f), fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Icon(icon, contentDescription = null, tint = content.copy(alpha = 0.78f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RecyclingHistoryTabs(
    tabs: List<RecyclingHistoryTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
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
                    color = if (selected) Color(0xFF006B1B) else Color.Transparent,
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
private fun HistoryLogCard(log: RecyclingLog) {
    val material = materialStyle(log.material_type)
    val status = statusStyle(log)
    val rejected = log.status.equals("Rejected", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = material.color.copy(alpha = if (rejected) 0.10f else 0.14f)
                    ) {
                        Icon(
                            material.icon,
                            contentDescription = material.title,
                            tint = if (rejected) Color(0xFF747776) else material.color,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(shape = CircleShape, color = status.color.copy(alpha = 0.12f)) {
                            Text(
                                text = status.label.uppercase(),
                                color = status.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            text = material.title,
                            color = if (rejected) Color(0xFF747776) else Color(0xFF2C2F2E),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Campus Recycling Deposit",
                            color = Color(0xFF595C5B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = formatLogDate(log.created_at),
                    color = Color(0x99595C5B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End
                )
            }

            if (rejected && !log.admin_notes.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                RejectionReasonBox(log.admin_notes)
            }

            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF1EF)))
            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(
                        text = "${formatKg(log.quantity)} kg",
                        color = if (rejected) Color(0xFF747776) else Color(0xFF006B1B),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("WEIGHT", color = Color(0xFF595C5B), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(status.pointsText, color = status.color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = if (log.status.equals("Approved", ignoreCase = true)) "REWARD" else "POINTS",
                        color = Color(0xFF595C5B),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RejectionReasonBox(reason: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0x0DB02500),
        border = BorderStroke(1.dp, Color(0x22B02500))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("VALIDATION FAILED", color = Color(0xFFB02500), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text(reason ?: "Material could not be verified.", color = Color(0xCCB02500), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ViewMoreHistoryButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
        Button(
            onClick = onClick,
            modifier = Modifier.width(210.dp).height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6E9E7))
        ) {
            Text("View More Activity", color = Color(0xFF2C2F2E), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyHistoryState(tab: RecyclingHistoryTab) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "No ${tab.label.lowercase()} recycling records yet",
            color = Color(0xFF595C5B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(28.dp).fillMaxWidth()
        )
    }
}

private data class MaterialHistoryStyle(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

private data class LogStatusStyle(
    val label: String,
    val color: Color,
    val pointsText: String
)

private fun materialStyle(material: String): MaterialHistoryStyle {
    return when (material.lowercase()) {
        "plastic" -> MaterialHistoryStyle("Plastic", Icons.Default.LocalDrink, Color(0xFF006A38))
        "paper" -> MaterialHistoryStyle("Paper", Icons.Default.Description, Color(0xFFFBC02D))
        "glass" -> MaterialHistoryStyle("Glass", Icons.Default.WineBar, Color(0xFF00656F))
        "metal" -> MaterialHistoryStyle("Metal", Icons.Default.Hardware, Color(0xFF747776))
        else -> MaterialHistoryStyle(material.replaceFirstChar { it.uppercase() }, Icons.Default.Recycling, Color(0xFF006B1B))
    }
}

private fun statusStyle(log: RecyclingLog): LogStatusStyle {
    return when {
        log.status.equals("Approved", ignoreCase = true) -> LogStatusStyle("Approved", Color(0xFF006A38), "+${log.points_awarded} pts")
        log.status.equals("Rejected", ignoreCase = true) -> LogStatusStyle("Rejected", Color(0xFFB02500), "0 pts")
        else -> LogStatusStyle("Pending", Color(0xFFFBC02D), "In Review")
    }
}

private fun RecyclingLog.isPendingStatus(): Boolean {
    return !status.equals("Approved", ignoreCase = true) && !status.equals("Rejected", ignoreCase = true)
}

private fun RecyclingLog.isPastStatus(): Boolean {
    return status.equals("Approved", ignoreCase = true) || status.equals("Rejected", ignoreCase = true)
}

private fun formatKg(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else String.format("%.1f", value)
}

private fun formatLogDate(value: String?): String {
    return value?.take(10)?.substring(5)?.replace("-", " ")?.uppercase() ?: "RECENT"
}



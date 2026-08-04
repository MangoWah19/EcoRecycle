package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fyp1.MainViewModel
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendMission
import com.example.fyp1.api.BackendSubmission
import com.example.fyp1.api.MissionSelectionCache
import com.example.fyp1.api.MissionRepository
import com.example.fyp1.components.EcoNavigationDrawer
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.offline.ConnectionModeChip
import com.example.fyp1.offline.rememberConnectionUiMode
import java.time.Instant
import java.time.format.DateTimeParseException

private val MissionTabs = listOf("All", "Available to Join", "Ongoing", "Pending Upload", "Pending Review", "Approved Proofs", "Completed Mission")

private enum class MissionUiStatus {
    AvailableToJoin,
    Ongoing,
    PendingUpload,
    Pending,
    PendingReview,
    ApprovedProofs,
    Rejected,
    Completed,
    Expired
}

@Composable
fun MissionsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val connectionMode = rememberConnectionUiMode()
    LaunchedEffect(Unit) { viewModel.fetchUserData(context) }

    val missionRepository = remember { MissionRepository(context) }
    var backendMissions by remember { mutableStateOf<List<BackendMission>>(emptyList()) }
    var mySubmissions by remember { mutableStateOf<List<BackendSubmission>>(emptyList()) }
    var isLoadingMissions by remember { mutableStateOf(false) }
    var missionError by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMissionTab by remember { mutableStateOf("All") }
    val visibleBackendMissions = backendMissions.filter { mission ->
        val query = searchQuery.trim()
        val status = missionStatusFor(mission, mySubmissions.forMission(mission.id))
        missionMatchesTab(status, selectedMissionTab) && (query.isBlank() ||
            mission.title.contains(query, ignoreCase = true) ||
            mission.description.contains(query, ignoreCase = true) ||
            mission.type.contains(query, ignoreCase = true))
    }

    LaunchedEffect(Unit) {
        isLoadingMissions = true
        missionError = null
        when (val result = missionRepository.getMissions()) {
            is AuthResult.Success -> {
                backendMissions = result.value
            }
            is AuthResult.Error -> {
                missionError = result.message
            }
        }
        when (val result = missionRepository.getMySubmissions()) {
            is AuthResult.Success -> {
                mySubmissions = result.value
            }
            is AuthResult.Error -> {
                if (missionError == null) missionError = result.message
            }
        }
        isLoadingMissions = false
    }

    EcoNavigationDrawer(navController = navController) { openDrawer ->
    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MissionBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { MissionTopBar(connectionMode = connectionMode, onMenuClick = openDrawer, onProfileClick = { navController.navigate("profile") }) }
            item { MissionHeader() }
            item {
                MissionSearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedTab = selectedMissionTab,
                    onTabSelected = { selectedMissionTab = it }
                )
            }
            if (isLoadingMissions) {
                item { MissionInfoMessage("Loading missions...") }
            }
            missionError?.let { error ->
                item { MissionInfoMessage(error) }
            }
            if (!isLoadingMissions && missionError == null && visibleBackendMissions.isEmpty()) {
                item { MissionInfoMessage("No missions found.") }
            }
            items(visibleBackendMissions) { mission ->
                val missionSubmissions = mySubmissions.forMission(mission.id)
                BackendMissionCard(
                    mission = mission,
                    status = missionStatusFor(mission, missionSubmissions),
                    onClick = {
                        MissionSelectionCache.selectedMission = mission
                        navController.navigate("mission_details/${mission.id}")
                    }
                )
            }
    }
    }
    }
}

@Composable
private fun BackendMissionCard(mission: BackendMission, status: MissionUiStatus, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column {
            MissionCardHero(mission = mission, onClick = onClick)
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    MissionTypeLabel(mission.type)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${mission.points} pts", color = MissionPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    mission.description,
                    color = MissionMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                MissionButton(text = "View Details", primary = status == MissionUiStatus.AvailableToJoin || status == MissionUiStatus.Ongoing, enabled = true, onClick = onClick)
            }
        }
    }
}

@Composable
private fun MissionCardHero(mission: BackendMission, onClick: () -> Unit) {
    val imageRequest = rememberEcoImageRequest(mission.imageUrl)
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clickable(onClick = onClick)) {
        EcoLoadingImage(
            model = imageRequest,
            contentDescription = mission.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallbackIcon = Icons.Default.Eco
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.54f))))
        )
        Text(
            text = mission.title,
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 15.dp),
            color = Color.White,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MissionTypeLabel(type: String) {
    Surface(shape = CircleShape, color = Color(0xFFE2F6E6)) {
        Text(
            text = missionTypeLabel(type),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MissionPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun MissionInfoMessage(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MissionSoftSurface) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MissionMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MissionTopBar(connectionMode: com.example.fyp1.offline.ConnectionUiMode, onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MissionPrimary)
            }
            Text("Eco-Recycle", color = MissionPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionModeChip(connectionMode)
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onProfileClick
                    ),
                shape = CircleShape,
                color = Color(0xFFE6E9E7),
                border = BorderStroke(2.dp, Color(0x1A006B1B))
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = MissionPrimary, modifier = Modifier.padding(9.dp))
            }
        }
    }
}

@Composable
private fun MissionHeader() {
    Column(modifier = Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Browse Missions", color = MissionPrimary, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
        Text("Join the green revolution on campus.", color = MissionMuted, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MissionSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), color = MissionSoftSurface) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF747776), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "Search sustainability tasks...",
                            color = Color(0xFF747776),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MissionTabs.forEach { tab ->
                MissionFilterChip(label = tab, selected = selectedTab == tab, onClick = { onTabSelected(tab) })
            }
        }
    }
}

@Composable
private fun MissionFilterChip(label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = CircleShape, color = if (selected) MissionPrimary else MissionSoftSurface) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = if (selected) Color.White else MissionMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MissionButton(text: String, primary: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = when {
            !enabled -> MissionSoftSurface
            primary -> MissionPrimary
            else -> MissionSoftSurface
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = if (primary && enabled) Color.White else MissionPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private val MissionBackground = Color(0xFFF5F7F5)
private val MissionPrimary = Color(0xFF006B1B)
private val MissionText = Color(0xFF2C2F2E)
private val MissionMuted = Color(0xFF686E6B)
private val MissionSoftSurface = Color(0xFFE6EDE9)

private fun missionStatusFor(mission: BackendMission, submissions: List<BackendSubmission>): MissionUiStatus {
    if (mission.isCompletedBy(submissions)) {
        return MissionUiStatus.Completed
    }

    return when (submissions.latestForMission(mission.id)?.status?.uppercase()) {
        "ONGOING" -> MissionUiStatus.Ongoing
        "PENDING_UPLOAD", "UPLOADING", "FAILED_UPLOAD" -> MissionUiStatus.PendingUpload
        "PENDING_REVIEW" -> MissionUiStatus.PendingReview
        "APPROVED" -> MissionUiStatus.ApprovedProofs
        "REJECTED" -> MissionUiStatus.Rejected
        "PENDING" -> MissionUiStatus.Pending
        else -> if (mission.hasExpired()) MissionUiStatus.Expired else MissionUiStatus.AvailableToJoin
    }
}

private fun List<BackendSubmission>.forMission(missionId: String): List<BackendSubmission> {
    return filter { it.missionId == missionId }
}

private fun List<BackendSubmission>.latestForMission(missionId: String): BackendSubmission? {
    return filter { it.missionId == missionId }
        .maxByOrNull { it.submittedAt ?: it.createdAt ?: "" }
}

private fun missionMatchesTab(status: MissionUiStatus, tab: String): Boolean = when (tab) {
    "All" -> true
    "Available to Join" -> status == MissionUiStatus.AvailableToJoin
    "Ongoing" -> status == MissionUiStatus.Ongoing
    "Pending Upload" -> status == MissionUiStatus.PendingUpload
    "Pending Review" -> status == MissionUiStatus.PendingReview
    "Approved Proofs" -> status == MissionUiStatus.ApprovedProofs
    "Completed Mission" -> status == MissionUiStatus.Completed
    else -> true
}

private fun BackendMission.isCompletedBy(submissions: List<BackendSubmission>): Boolean {
    val approvedSubmissions = submissions.filter { it.status.equals("APPROVED", ignoreCase = true) }
    return when (type) {
        "QUANTITY_BASED" -> {
            val target = targetQuantity ?: return false
            approvedSubmissions.sumOf { it.quantity ?: 0 } >= target
        }
        "STREAK_BASED" -> {
            val target = targetDays ?: return false
            approvedSubmissions.size >= target
        }
        "TIME_LIMITED" -> approvedSubmissions.isNotEmpty()
        else -> false
    }
}

private fun BackendMission.hasExpired(): Boolean {
    return try {
        Instant.parse(endAt).isBefore(Instant.now())
    } catch (_: DateTimeParseException) {
        false
    }
}

private fun missionTypeLabel(type: String): String = when (type) {
    "QUANTITY_BASED" -> "QUANTITY BASED"
    "STREAK_BASED" -> "STREAK BASED"
    "TIME_LIMITED" -> "TIME LIMITED"
    else -> type.replace('_', ' ').ifBlank { "MISSION" }
}

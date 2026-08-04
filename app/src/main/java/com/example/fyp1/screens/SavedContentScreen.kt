package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.api.BackendContent
import com.example.fyp1.api.ContentSelectionCache
import com.example.fyp1.api.SavedContentRepository
import com.example.fyp1.components.AppPopOutDialog
import com.example.fyp1.components.AppPopOutMessage
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.components.PopOutMessageType
import com.example.fyp1.offline.ConnectionModeChip
import com.example.fyp1.offline.ConnectionUiMode
import com.example.fyp1.offline.rememberConnectionUiMode
import kotlinx.coroutines.launch

private val SavedBackground = Color(0xFFF5F7F5)
private val SavedPrimary = Color(0xFF006B1B)
private val SavedSurface = Color.White
private val SavedSoftSurface = Color(0xFFEFF3F0)
private val SavedText = Color(0xFF2C2F2E)
private val SavedMuted = Color(0xFF747776)

private val SavedContentFilters = listOf(
    SavedContentFilter("All", null),
    SavedContentFilter("Plastic", "plastic"),
    SavedContentFilter("Paper", "paper"),
    SavedContentFilter("E-Waste", "ewaste"),
    SavedContentFilter("Food Waste", "food-waste"),
    SavedContentFilter("Sorting", "sorting"),
    SavedContentFilter("Cleanliness", "cleanliness"),
    SavedContentFilter("Safety", "safety"),
    SavedContentFilter("General", "general")
)

private data class SavedContentFilter(val label: String, val tag: String?)

@Composable
fun SavedContentScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val connectionMode = rememberConnectionUiMode()
    val repository = remember { SavedContentRepository(context) }
    val authRepository = remember { AuthRepository(context) }
    var savedContent by remember { mutableStateOf<List<BackendContent>>(emptyList()) }
    var savedContentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SavedContentFilters.first()) }
    var popOutMessage by remember { mutableStateOf<AppPopOutMessage?>(null) }

    suspend fun reloadSavedContent() {
        savedContent = repository.getSavedContent()
        savedContentIds = repository.getSavedIds()
    }

    LaunchedEffect(Unit) {
        reloadSavedContent()
    }

    val visibleContent = savedContent.filter { content ->
        val query = searchQuery.trim()
        val matchesFilter = selectedFilter.tag == null || content.tags.any { it == selectedFilter.tag }
        val matchesSearch = query.isBlank() ||
            content.title.contains(query, ignoreCase = true) ||
            content.body.contains(query, ignoreCase = true) ||
            content.summary.orEmpty().contains(query, ignoreCase = true) ||
            content.tags.any { it.contains(query, ignoreCase = true) }

        matchesFilter && matchesSearch
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SavedBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SavedContentTopBar(
                    connectionMode = connectionMode,
                    onBack = { navController.popBackStack() }
                )
            }

            item {
                SavedContentHeader(savedCount = savedContent.size)
            }

            item {
                SavedContentSearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (savedContent.isEmpty()) {
                item { EmptySavedContentState() }
            } else if (visibleContent.isEmpty()) {
                item { SavedContentInfoMessage("No saved content matches your search.") }
            } else {
                items(visibleContent, key = { it.id }) { content ->
                    SavedContentCard(
                        content = content,
                        isOffline = connectionMode == ConnectionUiMode.Offline,
                        onReadGuide = {
                            ContentSelectionCache.selectedContent = content
                            navController.navigate("content_detail/${content.id}")
                        },
                        isSaved = savedContentIds.contains(content.id),
                        onBookmarkToggle = {
                            scope.launch {
                                val saved = repository.toggle(content)
                                savedContentIds = if (saved) {
                                    savedContentIds + content.id
                                } else {
                                    savedContentIds - content.id
                                }
                            }
                        },
                        onTakeQuiz = {
                            when {
                                !authRepository.isLoggedIn() -> {
                                    popOutMessage = AppPopOutMessage(
                                        title = "Login Required",
                                        message = "Please log in before taking quizzes so your quiz result can be saved.",
                                        type = PopOutMessageType.Info
                                    )
                                }
                                connectionMode == ConnectionUiMode.Offline -> {
                                    popOutMessage = AppPopOutMessage(
                                        title = "Internet Required",
                                        message = "Please reconnect to the internet before taking this quiz. You can still read saved content while offline.",
                                        type = PopOutMessageType.Info
                                    )
                                }
                                else -> {
                                    ContentSelectionCache.selectedContent = content
                                    navController.navigate("quiz_attempt/${content.id}")
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        }
    }

    AppPopOutDialog(
        message = popOutMessage,
        onDismiss = { popOutMessage = null }
    )
}

@Composable
private fun SavedContentTopBar(
    connectionMode: ConnectionUiMode,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SavedPrimary)
            }
            Text(
                text = "Saved Content",
                color = SavedPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        ConnectionModeChip(connectionMode)
    }
}

@Composable
private fun SavedContentHeader(savedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SavedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E9E7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("YOUR LIBRARY", color = SavedMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Text(
                    text = "$savedCount Saved Guide${if (savedCount == 1) "" else "s"}",
                    color = SavedPrimary,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Read your bookmarked sustainability guides online or offline.",
                    color = SavedMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Surface(modifier = Modifier.size(54.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF86FAAC)) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = SavedPrimary, modifier = Modifier.padding(14.dp))
            }
        }
    }
}

@Composable
private fun SavedContentSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: SavedContentFilter,
    onFilterSelected: (SavedContentFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), color = SavedSoftSurface) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = SavedMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "Search saved guides...",
                            color = SavedMuted,
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

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SavedContentFilters.forEach { filter ->
                Surface(
                    modifier = Modifier.clickable { onFilterSelected(filter) },
                    shape = CircleShape,
                    color = if (selectedFilter == filter) SavedPrimary else SavedSoftSurface
                ) {
                    Text(
                        text = filter.label,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = if (selectedFilter == filter) Color.White else SavedMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedContentCard(
    content: BackendContent,
    isOffline: Boolean,
    isSaved: Boolean,
    onReadGuide: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onTakeQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SavedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column {
            SavedContentHero(content = content, isSaved = isSaved, onBookmarkToggle = onBookmarkToggle)
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SavedContentTagsAndTime(content)
                Text(
                    text = content.title,
                    color = SavedText,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = content.summary?.takeIf { it.isNotBlank() } ?: content.body,
                    color = SavedMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                SavedContentButton("Read Guide", primary = true, onClick = onReadGuide)
                SavedContentButton("Take Quiz", primary = false, offline = isOffline, onClick = onTakeQuiz)
            }
        }
    }
}

@Composable
private fun SavedContentHero(content: BackendContent, isSaved: Boolean, onBookmarkToggle: () -> Unit) {
    val imageRequest = rememberEcoImageRequest(content.imageUrl)
    Box(modifier = Modifier.fillMaxWidth().height(156.dp)) {
        EcoLoadingImage(
            model = imageRequest,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallbackIcon = Icons.Default.Eco
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.02f), Color.Black.copy(alpha = 0.22f)))))
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(36.dp).clickable(onClick = onBookmarkToggle),
            shape = CircleShape,
            color = if (isSaved) SavedPrimary else Color.White.copy(alpha = 0.9f),
            border = if (isSaved) null else BorderStroke(1.5.dp, SavedPrimary)
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Remove saved content" else "Save content",
                tint = if (isSaved) Color.White else SavedPrimary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun SavedContentTagsAndTime(content: BackendContent) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tags = content.tags.ifEmpty { listOf("general") }
            tags.forEach { tag -> SavedContentTag(contentTagLabel(tag)) }
        }
        Spacer(Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = SavedPrimary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("${content.estimatedReadMinutes ?: 5} min", color = SavedPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SavedContentTag(tag: String) {
    Surface(shape = CircleShape, color = Color(0xFFE2F6E6)) {
        Text(
            text = tag.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = SavedPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun SavedContentButton(text: String, primary: Boolean, offline: Boolean = false, onClick: () -> Unit) {
    val accent = if (offline) Color(0xFF9DA6A1) else SavedPrimary
    Surface(
        modifier = Modifier.fillMaxWidth().height(44.dp).clip(CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (primary) SavedPrimary else Color.Transparent,
        border = if (primary) null else BorderStroke(1.dp, accent)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (!primary) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = if (primary) Color.White else accent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptySavedContentState() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = SavedSurface, border = BorderStroke(1.dp, Color(0xFFE6E9E7))) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = SavedPrimary.copy(alpha = 0.62f), modifier = Modifier.size(34.dp))
            Text("No saved content yet", color = SavedText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Tap the bookmark icon on any learning guide to save it here for quick access.",
                color = SavedMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SavedContentInfoMessage(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SavedSoftSurface) {
        Text(message, modifier = Modifier.padding(16.dp), color = SavedMuted, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    }
}

private fun contentTagLabel(tag: String): String = when (tag) {
    "ewaste" -> "E-Waste"
    "food-waste" -> "Food Waste"
    else -> tag.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

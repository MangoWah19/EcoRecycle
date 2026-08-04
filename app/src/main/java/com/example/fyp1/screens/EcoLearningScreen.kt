package com.example.fyp1.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendContent
import com.example.fyp1.api.ContentRepository
import com.example.fyp1.api.ContentSelectionCache
import com.example.fyp1.api.SavedContentRepository
import com.example.fyp1.components.AppPopOutDialog
import com.example.fyp1.components.AppPopOutMessage
import com.example.fyp1.components.EcoNavigationDrawer
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.components.PopOutMessageType
import com.example.fyp1.offline.ConnectionModeChip
import com.example.fyp1.offline.ConnectionUiMode
import com.example.fyp1.offline.rememberConnectionUiMode
import kotlinx.coroutines.launch

private val ContentFilterOptions = listOf(
    ContentFilter("All", null),
    ContentFilter("Plastic", "plastic"),
    ContentFilter("Paper", "paper"),
    ContentFilter("E-Waste", "ewaste"),
    ContentFilter("Food Waste", "food-waste"),
    ContentFilter("Sorting", "sorting"),
    ContentFilter("Cleanliness", "cleanliness"),
    ContentFilter("Safety", "safety"),
    ContentFilter("General", "general")
)

private data class ContentFilter(val label: String, val tag: String?)

@Composable
fun EcoLearningScreen(navController: NavController) {
    val context = LocalContext.current
    val connectionMode = rememberConnectionUiMode()
    val contentRepository = remember { ContentRepository(context) }
    val savedContentRepository = remember { SavedContentRepository(context) }
    val authRepository = remember { AuthRepository(context) }
    var contentItems by remember { mutableStateOf<List<BackendContent>>(emptyList()) }
    var savedContentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ContentFilterOptions.first()) }
    var popOutMessage by remember { mutableStateOf<AppPopOutMessage?>(null) }

    val visibleContent = contentItems.filter { content ->
        val query = searchQuery.trim()
        val matchesFilter = selectedFilter.tag == null || content.tags.any { it == selectedFilter.tag }
        val matchesSearch = query.isBlank() ||
            content.title.contains(query, ignoreCase = true) ||
            content.body.contains(query, ignoreCase = true) ||
            content.summary.orEmpty().contains(query, ignoreCase = true) ||
            content.tags.any { it.contains(query, ignoreCase = true) }

        matchesFilter && matchesSearch
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        when (val result = contentRepository.getContent()) {
            is AuthResult.Success -> contentItems = result.value
            is AuthResult.Error -> errorMessage = result.message
        }
        savedContentIds = savedContentRepository.getSavedIds()
        isLoading = false
    }

    EcoNavigationDrawer(navController = navController) { openDrawer ->
    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LearnBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { EcoLearningTopBar(connectionMode = connectionMode, onMenuClick = openDrawer, onProfileClick = { navController.navigate("profile") }) }
            item { EcoLearningHeader() }
            item {
                SearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            if (isLoading) {
                item { ContentInfoMessage("Loading content...") }
            }
            errorMessage?.let { error ->
                item { ContentInfoMessage(error) }
            }
            if (!isLoading && errorMessage == null && visibleContent.isEmpty()) {
                item { ContentInfoMessage("No content found.") }
            }
            items(visibleContent, key = { it.id }) { content ->
                LearningGuideCard(
                    content = content,
                    isOffline = connectionMode == ConnectionUiMode.Offline,
                    isSaved = savedContentIds.contains(content.id),
                    onBookmarkToggle = {
                        val saved = savedContentRepository.toggle(content)
                        savedContentIds = if (saved) {
                            savedContentIds + content.id
                        } else {
                            savedContentIds - content.id
                        }
                    },
                    onReadGuide = {
                        ContentSelectionCache.selectedContent = content
                        navController.navigate("content_detail/${content.id}")
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
                                    message = "Please reconnect to the internet before taking this quiz. You can still read cached learning content while offline.",
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
    }
    }

    AppPopOutDialog(
        message = popOutMessage,
        onDismiss = { popOutMessage = null }
    )
}

@Composable
private fun EcoLearningTopBar(connectionMode: ConnectionUiMode, onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = LearnPrimary)
            }
            Text(
                text = "Eco-Recycle",
                color = LearnPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = LearnPrimary,
                    modifier = Modifier.padding(9.dp)
                )
            }
        }
    }
}

@Composable
private fun EcoLearningHeader() {
    Column(
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Eco-Learning",
            color = LearnPrimary,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Master the art of sustainable living.",
            color = LearnMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: ContentFilter,
    onFilterSelected: (ContentFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            color = LearnSoftSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF747776), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search guides, tips, or topics",
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

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ContentFilterOptions.forEach { filter ->
                FilterChipLabel(
                    label = filter.label,
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
private fun FilterChipLabel(label: String, selected: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) LearnPrimary else LearnSoftSurface
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = if (selected) Color.White else LearnMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LearningGuideCard(
    content: BackendContent,
    isOffline: Boolean,
    isSaved: Boolean,
    onBookmarkToggle: suspend () -> Unit,
    onReadGuide: () -> Unit,
    onTakeQuiz: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LearnSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column {
            ContentCardHero(
                content = content,
                isSaved = isSaved,
                onBookmarkToggle = { scope.launch { onBookmarkToggle() } }
            )
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContentTagsAndTime(content)
                Text(
                    text = content.title,
                    color = LearnText,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = content.summary?.takeIf { it.isNotBlank() } ?: content.body,
                    color = LearnMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                LearningButton(text = "Read Guide", primary = true, onClick = onReadGuide)
                LearningButton(text = "Take Quiz", primary = false, offline = isOffline, onClick = onTakeQuiz)
            }
        }
    }
}

@Composable
private fun ContentCardHero(content: BackendContent, isSaved: Boolean, onBookmarkToggle: () -> Unit) {
    val imageRequest = rememberEcoImageRequest(content.imageUrl)
    Box(modifier = Modifier.fillMaxWidth().height(156.dp)) {
        EcoLoadingImage(
            model = imageRequest,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallbackIcon = Icons.Default.Eco
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.02f), Color.Black.copy(alpha = 0.22f))))
        )
        BookmarkButton(
            isSaved = isSaved,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            onClick = onBookmarkToggle
        )
    }
}

@Composable
private fun ContentTagsAndTime(content: BackendContent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tags = content.tags.ifEmpty { listOf("general") }
            tags.forEach { tag -> TagLabel(contentTagLabel(tag)) }
        }
        Spacer(Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = LearnPrimary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${content.estimatedReadMinutes ?: 5} min",
                color = LearnPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BookmarkButton(isSaved: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.size(36.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSaved) LearnPrimary else Color.White.copy(alpha = 0.9f),
        border = if (isSaved) null else BorderStroke(1.5.dp, LearnPrimary)
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isSaved) "Remove saved content" else "Save content",
            tint = if (isSaved) Color.White else LearnPrimary,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun TagLabel(tag: String) {
    Surface(shape = CircleShape, color = Color(0xFFE2F6E6)) {
        Text(
            text = tag.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = LearnPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun LearningButton(text: String, primary: Boolean, offline: Boolean = false, onClick: () -> Unit) {
    val accent = if (offline) Color(0xFF9DA6A1) else LearnPrimary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (primary) LearnPrimary else Color.Transparent,
        border = if (primary) null else BorderStroke(1.dp, accent)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!primary) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (primary) Color.White else accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ContentInfoMessage(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = LearnSoftSurface) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = LearnMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun contentTagLabel(tag: String): String = when (tag) {
    "ewaste" -> "E-Waste"
    "food-waste" -> "Food Waste"
    else -> tag.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private val LearnBackground = Color(0xFFF5F7F5)
private val LearnPrimary = Color(0xFF006B1B)
private val LearnText = Color(0xFF2C2F2E)
private val LearnMuted = Color(0xFF595C5B)
private val LearnSurface = Color.White
private val LearnSoftSurface = Color(0xFFE6E9E7)

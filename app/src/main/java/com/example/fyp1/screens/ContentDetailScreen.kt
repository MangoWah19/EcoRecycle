package com.example.fyp1.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendContent
import com.example.fyp1.api.BackendContentBlock
import com.example.fyp1.api.ContentRepository
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

@Composable
fun ContentDetailScreen(navController: NavController, contentId: String) {
    val context = LocalContext.current
    val connectionMode = rememberConnectionUiMode()
    val repository = remember { ContentRepository(context) }
    val savedContentRepository = remember { SavedContentRepository(context) }
    val authRepository = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    val cachedContent = remember(contentId) {
        ContentSelectionCache.selectedContent?.takeIf { it.id == contentId }
    }
    var content by remember(contentId) { mutableStateOf(cachedContent) }
    var isLoading by remember(contentId) { mutableStateOf(cachedContent == null) }
    var errorMessage by remember(contentId) { mutableStateOf<String?>(null) }
    var expanded by remember(contentId) { mutableStateOf(false) }
    var popOutMessage by remember(contentId) { mutableStateOf<AppPopOutMessage?>(null) }
    var isSaved by remember(contentId) { mutableStateOf(false) }

    LaunchedEffect(contentId) {
        isLoading = content == null
        errorMessage = null
        when (val result = repository.getContentById(contentId)) {
            is AuthResult.Success -> content = result.value
            is AuthResult.Error -> errorMessage = result.message
        }
        isSaved = savedContentRepository.isSaved(contentId)
        isLoading = false
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ArticleBackground)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ContentDetailTopBar(
                    connectionMode = connectionMode,
                    isSaved = isSaved,
                    onBack = { navController.popBackStack() },
                    onBookmarkClick = {
                        val currentContent = content
                        if (currentContent != null) {
                            scope.launch {
                                isSaved = savedContentRepository.toggle(currentContent)
                            }
                        }
                    }
                )
            }

            if (isLoading) {
                item {
                    ArticleInfoMessage(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        message = "Loading content..."
                    )
                }
                return@LazyColumn
            }

            if (errorMessage != null && content == null) {
                item {
                    ArticleInfoMessage(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        message = errorMessage ?: "Could not load content."
                    )
                }
                return@LazyColumn
            }

            content?.let { currentContent ->
                val blocks = currentContent.articleBlocks()
                val visibleBlocks = if (expanded) blocks else blocks.take(3)

                item { ContentHero(currentContent) }
                item {
                    ArticleMetaRow(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        content = currentContent
                    )
                }
                item {
                    ArticleExcerpt(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        text = currentContent.summary?.takeIf { it.isNotBlank() } ?: currentContent.body
                    )
                }
                item {
                    DidYouKnowCard(
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
                items(visibleBlocks) { block ->
                    ArticleContentBlock(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        block = block
                    )
                }
                if (!expanded && blocks.size > visibleBlocks.size) {
                    item {
                        ReadMoreButton(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            onClick = { expanded = true }
                        )
                    }
                }
                item {
                    TakeQuizCallout(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        isOffline = connectionMode == ConnectionUiMode.Offline,
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
                                    ContentSelectionCache.selectedContent = currentContent
                                    navController.navigate("quiz_attempt/${currentContent.id}")
                                }
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }
    }

    AppPopOutDialog(
        message = popOutMessage,
        onDismiss = { popOutMessage = null }
    )
}

@Composable
private fun ContentDetailTopBar(
    connectionMode: ConnectionUiMode,
    isSaved: Boolean,
    onBack: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ArticlePrimary)
        }
        Text(
            "Learning Guide",
            color = ArticlePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp).weight(1f)
        )
        ConnectionModeChip(connectionMode, modifier = Modifier.padding(end = 8.dp))
        Surface(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .clickable(onClick = onBookmarkClick),
            shape = CircleShape,
            color = if (isSaved) ArticlePrimary else Color.White,
            border = if (isSaved) null else BorderStroke(1.5.dp, ArticlePrimary)
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Remove saved content" else "Save content",
                tint = if (isSaved) Color.White else ArticlePrimary,
                modifier = Modifier.padding(9.dp)
            )
        }
    }
}

@Composable
private fun ContentHero(content: BackendContent) {
    val imageRequest = rememberEcoImageRequest(content.imageUrl)
    Box(modifier = Modifier.fillMaxWidth().height(310.dp)) {
        EcoLoadingImage(
            model = imageRequest,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallbackIcon = Icons.AutoMirrored.Filled.MenuBook
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArticleTagRow(content.tags.ifEmpty { listOf("general") })
            Text(
                text = content.title,
                color = Color.White,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ArticleMetaRow(modifier: Modifier = Modifier, content: BackendContent) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = ArticlePrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("${content.estimatedReadMinutes ?: 5} min read", color = ArticleMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ArticleExcerpt(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(ArticlePrimary, RoundedCornerShape(2.dp))
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 18.dp, end = 4.dp),
            color = ArticleText,
            fontSize = 16.sp,
            lineHeight = 25.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DidYouKnowCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFEFF3F1)
    ) {
        Box {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = ArticlePrimary.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(62.dp)
            )
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Did You Know?", color = ArticlePrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Recycling one aluminum can saves enough energy to run a small device for several hours.",
                    color = ArticleMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ArticleContentBlock(modifier: Modifier = Modifier, block: BackendContentBlock) {
    when (block.type.lowercase()) {
        "heading" -> {
            Text(
                text = block.text.orEmpty(),
                modifier = modifier.fillMaxWidth().padding(top = 8.dp),
                color = ArticleText,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        "image" -> {
            val imageRequest = rememberEcoImageRequest(block.url)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                EcoLoadingImage(
                    model = imageRequest,
                    contentDescription = block.alt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallbackIcon = Icons.AutoMirrored.Filled.MenuBook
                )
            }
        }
        else -> {
            Text(
                text = block.text.orEmpty(),
                modifier = modifier.fillMaxWidth(),
                color = ArticleMuted,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReadMoreButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .height(48.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, ArticlePrimary.copy(alpha = 0.2f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = ArticlePrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Read More", color = ArticlePrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun TakeQuizCallout(modifier: Modifier = Modifier, isOffline: Boolean, onTakeQuiz: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFE2F6E6)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ready to test your knowledge?",
                color = ArticlePrimary,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Take a quick quiz based on this article and earn impact points.",
                color = ArticleMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTakeQuiz),
                shape = CircleShape,
                color = if (isOffline) Color(0xFFB8C0BC) else ArticlePrimary
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Take Quiz", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ArticleTagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(shape = CircleShape, color = ArticlePrimary) {
                Text(
                    text = contentTagLabel(tag).uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color(0xFFD1FFC8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun ArticleInfoMessage(modifier: Modifier = Modifier, message: String) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = ArticleSoftSurface) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = ArticleMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun BackendContent.articleBlocks(): List<BackendContentBlock> {
    return contentBlocks?.takeIf { it.isNotEmpty() }
        ?: listOf(BackendContentBlock(type = "paragraph", text = body))
}

private fun contentTagLabel(tag: String): String = when (tag) {
    "ewaste" -> "E-Waste"
    "food-waste" -> "Food Waste"
    else -> tag.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private val ArticleBackground = Color(0xFFF5F7F5)
private val ArticlePrimary = Color(0xFF006B1B)
private val ArticleText = Color(0xFF2C2F2E)
private val ArticleMuted = Color(0xFF595C5B)
private val ArticleSoftSurface = Color(0xFFE6E9E7)

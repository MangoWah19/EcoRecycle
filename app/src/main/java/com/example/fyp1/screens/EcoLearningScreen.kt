package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.R
import com.example.fyp1.components.FloatingBottomNavigationScaffold

private val LearnBackground = Color(0xFFF5F7F5)
private val LearnPrimary = Color(0xFF006B1B)
private val LearnText = Color(0xFF2C2F2E)
private val LearnMuted = Color(0xFF595C5B)
private val LearnSurface = Color.White
private val LearnSoftSurface = Color(0xFFE6E9E7)

private data class LearningCardData(
    val category: String,
    val readingTime: String,
    val title: String,
    val description: String,
    val imageRes: Int? = null,
    val featured: Boolean = false
)

@Composable
fun EcoLearningScreen(navController: NavController) {
    val cards = listOf(
        LearningCardData(
            category = "COMPOSTING",
            readingTime = "12 min",
            title = "Campus Composting 101",
            description = "Turn your lunch scraps into campus fertilizer in 3 easy steps. Learn what goes in the green bin and what stays out.",
            imageRes = R.drawable.paper_keep_dry,
            featured = true
        ),
        LearningCardData(
            category = "RECYCLING",
            readingTime = "7 min",
            title = "Glass Recycling Mastery",
            description = "Which colors can be mixed? A complete guide for dorm residents on proper sorting and cleaning."
        ),
        LearningCardData(
            category = "MATERIALS",
            readingTime = "5 min",
            title = "The 7 Plastic Codes",
            description = "Stop guessing at the bin. Know your numbers from 1 to 7 and which ones are actually recyclable locally.",
            imageRes = R.drawable.plastic_step_2
        ),
        LearningCardData(
            category = "E-WASTE",
            readingTime = "Update",
            title = "Safe E-Waste Disposal",
            description = "Old laptop? Broken headphones? Find the nearest campus drop-off and learn why it matters."
        )
    )

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
            item { EcoLearningTopBar(onMenuClick = { }, onProfileClick = { navController.navigate("profile") }) }
            item { EcoLearningHeader() }
            item { SearchAndFilters() }
            cards.forEach { card ->
                item {
                    LearningGuideCard(
                        card = card,
                        onReadGuide = { /* TODO: Open guide detail screen */ },
                        onTakeQuiz = { /* TODO: Open quiz screen */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun EcoLearningTopBar(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
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
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onProfileClick),
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
private fun SearchAndFilters() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .weight(1f)
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
                    Text(
                        text = "Search guides, tips, or topics",
                        color = Color(0xFF747776),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(8.dp),
                color = LearnSoftSurface
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = LearnMuted, modifier = Modifier.padding(14.dp))
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChipLabel("All", selected = true)
            FilterChipLabel("Recycling")
            FilterChipLabel("Zero Waste")
            FilterChipLabel("Composting")
            FilterChipLabel("Energy")
        }
    }
}

@Composable
private fun FilterChipLabel(label: String, selected: Boolean = false) {
    Surface(
        shape = CircleShape,
        color = if (selected) LearnPrimary else LearnSoftSurface
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = if (selected) Color.White else LearnMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LearningGuideCard(
    card: LearningCardData,
    onReadGuide: () -> Unit,
    onTakeQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LearnSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (card.featured) null else BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column {
            if (card.imageRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (card.featured) 160.dp else 132.dp)
                ) {
                    Image(
                        painter = painterResource(id = card.imageRes),
                        contentDescription = card.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.18f))
                                )
                            )
                    )
                    if (card.featured) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp),
                            shape = CircleShape,
                            color = Color(0xFF11EAFE)
                        ) {
                            Text(
                                text = "FEATURED GUIDE",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color(0xFF003D43),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    BookmarkButton(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (card.imageRes == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) { BookmarkButton() }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryLabel(card.category)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = LearnPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(card.readingTime, color = LearnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = card.title,
                    color = LearnText,
                    fontSize = if (card.featured) 22.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = card.description,
                    color = LearnMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                LearningButton(text = "Read Guide", primary = true, onClick = onReadGuide)
                LearningButton(text = "Take Quiz", primary = false, onClick = onTakeQuiz)
            }
        }
    }
}

@Composable
private fun BookmarkButton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.86f)
    ) {
        Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = LearnPrimary, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun CategoryLabel(category: String) {
    Surface(shape = CircleShape, color = Color(0xFFE2F6E6)) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = LearnPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun LearningButton(text: String, primary: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (primary) LearnPrimary else Color.Transparent,
        border = if (primary) null else BorderStroke(1.dp, LearnPrimary)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!primary) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = LearnPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (primary) Color.White else LearnPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}


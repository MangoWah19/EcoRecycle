package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.components.FloatingBottomNavigationScaffold

private val InfoBackground = Color(0xFFF5F7F5)
private val InfoPrimary = Color(0xFF006B1B)
private val InfoSecondary = Color(0xFF006A38)
private val InfoTertiary = Color(0xFF00656F)
private val InfoText = Color(0xFF2C2F2E)
private val InfoMuted = Color(0xFF66706B)

@Composable
fun AboutAppScreen(navController: NavController) {
    InfoPageScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(InfoBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { InfoTopBar(title = "About This App", onBack = { navController.popBackStack() }) }
            item { AboutHero() }
            item {
                InfoFeatureCard(
                    icon = Icons.Default.Eco,
                    title = "Our Mission",
                    body = "EcoRecycle is currently in beta for live testing. It helps students turn everyday recycling, learning, and campus challenges into measurable environmental action. The app brings missions, recycling deposits, educational guides, quizzes, badges, and rewards into one student-friendly experience so sustainable habits feel clear, rewarding, and easy to track."
                )
            }
        }
    }
}

@Composable
fun HowItWorksScreen(navController: NavController) {
    InfoPageScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(InfoBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { InfoTopBar(title = "How It Works", onBack = { navController.popBackStack() }) }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "The Digital\nGreenhouse",
                        color = InfoPrimary,
                        fontSize = 34.sp,
                        lineHeight = 39.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "A quick guide to learning, recycling, joining missions, and tracking your impact.",
                        color = InfoMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }
            item {
                HowChapterCard(
                    chapter = "CHAPTER 01",
                    title = "Learn & Save Guides",
                    description = "Browse recycling guides, read educational content, save useful guides, and take quizzes when you are online.",
                    icon = Icons.Default.School,
                    background = Color(0xFFD8FBDD),
                    accent = InfoPrimary
                )
            }
            item {
                HowChapterCard(
                    chapter = "CHAPTER 02",
                    title = "Scan & Deposit",
                    description = "Submit recycling records with image proof, or scan a valid QR code issued by an admin for QR-based deposits.",
                    icon = Icons.Default.QrCodeScanner,
                    background = Color(0xFFDDFBE9),
                    accent = InfoSecondary
                )
            }
            item {
                HowChapterCard(
                    chapter = "CHAPTER 03",
                    title = "Join Missions",
                    description = "Join campus missions, upload required proof, and track mission progress based on the mission type.",
                    icon = Icons.Default.Recycling,
                    background = Color(0xFFCFF7FB),
                    accent = InfoTertiary
                )
            }
            item {
                HowChapterCard(
                    chapter = "CHAPTER 04",
                    title = "Earn Progress",
                    description = "Approved actions can create point events, unlock badges, and help you redeem available rewards.",
                    icon = Icons.Default.EmojiEvents,
                    background = Color(0xFFEAF3E8),
                    accent = InfoPrimary
                )
            }
        }
    }
}

@Composable
fun SustainabilityPolicyScreen(navController: NavController) {
    InfoPageScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(InfoBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { InfoTopBar(title = "Sustainability Policy", onBack = { navController.popBackStack() }) }
            item {
                PolicyHeader()
            }
            item {
                PolicyCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "Data Integrity",
                    body = "Recycling and mission submissions are reviewed through backend rules, admin approval, QR validation, and point-event records so student progress stays fair."
                )
            }
            item {
                PolicyCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin Review",
                    body = "Pending deposits and mission proofs can be checked by admins before points, mission progress, badges, or rewards are treated as completed."
                )
            }
            item {
                PolicyCard(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Access",
                    body = "Protected screens use the logged-in session token. Personal activity should only be shown to the correct student or authorized admin role."
                )
            }
            item {
                PolicyCard(
                    icon = Icons.Default.Bookmark,
                    title = "Saved Learning",
                    body = "Saved guides are kept for easier access and offline reading support. Removing a bookmark only affects the student's own saved list."
                )
            }
        }
    }
}

@Composable
private fun InfoPageScaffold(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    FloatingBottomNavigationScaffold(navController = navController, content = content)
}

@Composable
private fun InfoTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = InfoPrimary
            )
        }
        Text(
            text = title,
            color = InfoPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun AboutHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFFEAF7EA),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFEAF7EA), Color(0xFFD8F6DD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = InfoPrimary, modifier = Modifier.size(46.dp))
            }
        }
        Text("Eco-Recycle", color = InfoPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("BETA VERSION - LIVE TESTING", color = InfoMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
        Surface(
            modifier = Modifier
                .size(width = 92.dp, height = 6.dp),
            shape = CircleShape,
            color = Color(0xFF78EB9F)
        ) {}
    }
}

@Composable
private fun InfoFeatureCard(icon: ImageVector, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1EF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = InfoPrimary, modifier = Modifier.size(24.dp))
                Text(title, color = InfoPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(body, color = InfoMuted, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun HowChapterCard(
    chapter: String,
    title: String,
    description: String,
    icon: ImageVector,
    background: Color,
    accent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(topStart = 46.dp, bottomEnd = 46.dp, topEnd = 18.dp, bottomStart = 18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .padding(start = 18.dp)
                    .size(54.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(18.dp),
                color = accent
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(13.dp))
            }
            Column(
                modifier = Modifier.padding(start = 26.dp, end = 24.dp, top = 74.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(chapter, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                Text(title, color = InfoText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(description, color = InfoMuted, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun PolicyHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Sustainability Policy",
            color = InfoPrimary,
            fontSize = 30.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "A practical summary of how EcoRecycle keeps student recycling, learning progress, rewards, and mission records fair and trustworthy.",
            color = InfoMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFE7F5EA)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Policy, contentDescription = null, tint = InfoPrimary, modifier = Modifier.size(58.dp))
            }
        }
    }
}

@Composable
private fun PolicyCard(icon: ImageVector, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFD8FBDD)) {
                Icon(icon, contentDescription = null, tint = InfoPrimary, modifier = Modifier.padding(10.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, color = InfoPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF78C98F), modifier = Modifier.size(14.dp))
                }
                Text(body, color = InfoMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

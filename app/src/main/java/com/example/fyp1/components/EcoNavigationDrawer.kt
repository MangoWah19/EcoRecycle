package com.example.fyp1.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun EcoNavigationDrawer(
    navController: NavController,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    fun navigateToSavedContentFromProfile() {
        scope.launch {
            drawerState.close()
            navController.navigate("profile")
            navController.navigate("saved_content")
        }
    }

    fun navigateToProfileSubPage(route: String) {
        scope.launch {
            drawerState.close()
            navController.navigate("profile")
            navController.navigate(route)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EcoDrawerContent(
                onClose = ::closeDrawer,
                onAboutApp = { navigateToProfileSubPage("about_app") },
                onHowItWorks = { navigateToProfileSubPage("how_it_works") },
                onSustainabilityPolicy = { navigateToProfileSubPage("sustainability_policy") },
                onNotifications = { navigateToProfileSubPage("notifications") },
                onRecyclingGuide = { navigateToProfileSubPage("recycling_guide") },
                onSavedContent = ::navigateToSavedContentFromProfile
            )
        }
    ) {
        content {
            scope.launch { drawerState.open() }
        }
    }
}

@Composable
private fun EcoDrawerContent(
    onClose: () -> Unit,
    onAboutApp: () -> Unit,
    onHowItWorks: () -> Unit,
    onSustainabilityPolicy: () -> Unit,
    onNotifications: () -> Unit,
    onRecyclingGuide: () -> Unit,
    onSavedContent: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight(),
        drawerContainerColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Eco-Recycle",
                color = DrawerPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    ),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close drawer",
                    tint = DrawerMuted,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE8ECE9))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DrawerSectionTitle("GENERAL INFORMATION")
                DrawerRow(Icons.Default.Info, "About the App", onClick = onAboutApp)
                DrawerRow(Icons.AutoMirrored.Filled.HelpOutline, "How it Works", onClick = onHowItWorks)
                DrawerRow(Icons.Default.Policy, "Sustainability Policy", onClick = onSustainabilityPolicy)
                DrawerRow(Icons.Default.Notifications, "Notifications", onClick = onNotifications)
                DrawerRow(Icons.Default.Recycling, "Recycling Guide", onClick = onRecyclingGuide)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DrawerSectionTitle("YOUR CONTENT")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSavedContent
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = DrawerPrimary,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Saved Content",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Access all your bookmarked guides, quizzes, and educational materials here.",
                    color = DrawerMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        color = DrawerMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun DrawerRow(icon: ImageVector, text: String, onClick: (() -> Unit)? = null) {
    val rowModifier = if (onClick == null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFD4D9D6))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DrawerMuted,
                modifier = Modifier.padding(5.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            color = DrawerText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private val DrawerPrimary = Color(0xFF007A20)
private val DrawerText = Color(0xFF2C2F2E)
private val DrawerMuted = Color(0xFF6D7370)

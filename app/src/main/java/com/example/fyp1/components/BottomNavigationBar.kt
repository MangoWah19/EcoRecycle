package com.example.fyp1.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

object FloatingBottomNavigationDefaults {
    val BarHeight = 76.dp
    val BottomMargin = 20.dp

    @Composable
    fun contentBottomPadding(extraSpacing: androidx.compose.ui.unit.Dp = 18.dp): androidx.compose.ui.unit.Dp {
        return BarHeight + BottomMargin + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + extraSpacing
    }
}
private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val activeRoutes: Set<String> = setOf(route)
)

@Composable
fun FloatingBottomNavigationScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = topBar,
            bottomBar = {}
        ) { innerPadding ->
            val contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = innerPadding.calculateBottomPadding() +
                    FloatingBottomNavigationDefaults.contentBottomPadding()
            )
            content(contentPadding)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(bottom = FloatingBottomNavigationDefaults.BottomMargin)
        ) {
            BottomNavigationBar(navController = navController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("HOME", "home", Icons.Default.Home),
        BottomNavItem(
            label = "RECYCLE",
            route = "submit_recycling",
            icon = Icons.Default.Recycling,
            activeRoutes = setOf("submit_recycling", "qr_scanner")
        ),
        BottomNavItem(
            label = "MISSIONS",
            route = "missions",
            icon = Icons.Default.Stars,
            activeRoutes = setOf("missions", "mission_details/{missionType}")
        ),
        BottomNavItem(
            label = "LEARN",
            route = "eco_learning",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            activeRoutes = setOf("eco_learning", "content_detail/{contentId}", "quiz_attempt/{contentId}", "quiz_review")
        ),
        BottomNavItem("PROFILE", "profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(FloatingBottomNavigationDefaults.BarHeight),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE7EBE8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.activeRoutes.contains(currentRoute)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BottomNavPillItem(
                        label = item.label,
                        icon = item.icon,
                        selected = selected,
                        onClick = { if (!selected) navController.navigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavPillItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val pillColor = if (selected) Color(0xFF00751D) else Color.Transparent
    val contentColor = if (selected) Color.White else Color(0xFF747B77)

    Column(
        modifier = Modifier
            .width(if (selected) 84.dp else 60.dp)
            .height(58.dp)
            .background(pillColor, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(if (selected) 22.dp else 20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label.lowercase().replaceFirstChar { it.uppercase() },
            color = contentColor,
            fontSize = if (selected) 10.5.sp else 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}





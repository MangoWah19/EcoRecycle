package com.example.fyp1.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MaterialGuide

private val GuideBackground = Color(0xFFF5F7F5)
private val GuideGreen = Color(0xFF0B7D2B)
private val GuideCardGreen = Color(0xFF218D34)
private val GuideIconBackground = Color(0xFF92F08E)
private val GuideText = Color(0xFF343A38)
private val GuideMutedText = Color(0xFF6D7772)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingGuideScreen(navController: NavController) {
    val items = remember {
        listOf(
            MaterialGuide("Plastic", "50 pts/kg", GuideGreen, Icons.Filled.LocalDrink),
            MaterialGuide("Paper", "20 pts/kg", GuideGreen, Icons.Filled.Description),
            MaterialGuide("Glass", "30 pts/kg", GuideGreen, Icons.Filled.WineBar),
            MaterialGuide("Metal", "60 pts/kg", GuideGreen, Icons.Filled.Hardware)
        )
    }

    Scaffold(
        containerColor = GuideBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recycling Guide",
                        color = GuideGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GuideGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GuideBackground,
                    scrolledContainerColor = GuideBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GuideBackground),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "What are you recycling today?",
                        color = GuideText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Select a material to see how to prepare it correctly for the green bin.",
                        color = GuideMutedText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            item {
                GuideMaterialGrid(
                    materials = items,
                    onMaterialClick = { guide ->
                        navController.navigate("guide_detail/${guide.name}")
                    }
                )
            }

            item {
                RecyclingHackCard()
            }
        }
    }
}

@Composable
private fun GuideMaterialGrid(
    materials: List<MaterialGuide>,
    onMaterialClick: (MaterialGuide) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        materials.chunked(2).forEach { rowMaterials ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                rowMaterials.forEach { guide ->
                    MaterialButton(
                        guide = guide,
                        modifier = Modifier.weight(1f),
                        onClick = { onMaterialClick(guide) }
                    )
                }
                if (rowMaterials.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MaterialButton(
    guide: MaterialGuide,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(136.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(GuideIconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = guide.icon,
                    contentDescription = null,
                    tint = GuideGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = guide.name,
                color = GuideMutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecyclingHackCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GuideCardGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 16.dp, top = 22.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Recycling Hack",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep all materials loose in the bin. Plastic bags can jam sorting machines!",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}
package com.example.fyp1.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val DetailBackground = Color(0xFFF5F7F5)
private val DetailGreen = Color(0xFF0B7D2B)
private val DetailIconBackground = Color(0xFF92F08E)
private val DetailText = Color(0xFF343A38)
private val DetailMutedText = Color(0xFF6D7772)

private data class MaterialDetail(
    val name: String,
    val icon: ImageVector,
    val steps: List<String>,
    val note: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailScreen(navController: NavController, material: String) {
    val detail = remember(material) { materialDetails(material) }

    Scaffold(
        containerColor = DetailBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail.name,
                        color = DetailGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DetailGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailBackground,
                    scrolledContainerColor = DetailBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DetailBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(DetailIconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = detail.icon,
                                contentDescription = null,
                                tint = DetailGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Prepare ${detail.name} correctly",
                            color = DetailText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = detail.note,
                            color = DetailMutedText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Preparation Guide",
                            color = DetailText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        detail.steps.forEach { step ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = DetailGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = step,
                                    color = DetailMutedText,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DetailGreen)
                ) {
                    Text(
                        text = "Got it",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun materialDetails(material: String): MaterialDetail {
    return when (material.lowercase()) {
        "paper" -> MaterialDetail(
            name = "Paper",
            icon = Icons.Filled.Description,
            steps = listOf(
                "Keep paper clean and dry before placing it in the bin.",
                "Flatten boxes and remove plastic wrapping or tape where possible.",
                "Do not recycle paper with food residue, oil, or heavy stains."
            ),
            note = "Clean paper is easier to sort and recycle into new paper products."
        )
        "glass" -> MaterialDetail(
            name = "Glass",
            icon = Icons.Filled.WineBar,
            steps = listOf(
                "Rinse bottles and jars to remove leftover liquid or food.",
                "Remove caps or lids if they are made from another material.",
                "Keep broken glass separate for safer handling."
            ),
            note = "Clear, clean glass helps the campus team sort deposits faster."
        )
        "metal" -> MaterialDetail(
            name = "Metal",
            icon = Icons.Filled.Hardware,
            steps = listOf(
                "Empty cans completely before recycling.",
                "Rinse sticky or oily containers so they do not contaminate other materials.",
                "Crush cans when possible to save space in the bin."
            ),
            note = "Prepared metal containers take up less space and are easier to verify."
        )
        else -> MaterialDetail(
            name = "Plastic",
            icon = Icons.Filled.LocalDrink,
            steps = listOf(
                "Empty and rinse plastic bottles or containers.",
                "Remove leftover food residue before recycling.",
                "Keep plastic loose in the bin because plastic bags can jam sorting machines."
            ),
            note = "Clean plastic improves sorting accuracy and reduces rejected deposits."
        )
    }
}
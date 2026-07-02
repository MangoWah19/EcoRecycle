package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.R
import com.example.fyp1.components.FloatingBottomNavigationScaffold

@Composable
fun MissionDetailsScreen(navController: NavController, viewModel: MainViewModel, missionType: String) {
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        when (missionType) {
            "plastic_king" -> PlasticKingMissionDetailContent(
                padding = padding,
                progress = plasticKingProgress(viewModel),
                current = viewModel.plasticKg.toDouble(),
                target = 100.0,
                onBack = { navController.popBackStack() }
            )
            else -> CoffeeMissionDetailContent(
                padding = padding,
                onBack = { navController.popBackStack() },
                onJoinMission = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlasticKingMissionDetailContent(
    padding: PaddingValues,
    progress: Float,
    current: Double,
    target: Double,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MissionBackground)
            .padding(top = padding.calculateTopPadding())
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { MissionDetailTopBar(onBack) }
        item { MissionHero(R.drawable.mission_plastic_king, "Plastic King") }
        item { MissionMetaRow() }
        item { PlasticKingProgressCard(progress = progress, current = current, target = target) }
        item { PlasticKingAbout() }
        item { PlasticKingHowToCompleteCard() }
        item { Spacer(Modifier.height(10.dp)) }
    }
}

@Composable
private fun CoffeeMissionDetailContent(padding: PaddingValues, onBack: () -> Unit, onJoinMission: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MissionBackground)
            .padding(top = padding.calculateTopPadding())
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { MissionDetailTopBar(onBack) }
        item { MissionHero(R.drawable.mission_zero_waste_coffee, "Zero-Waste Coffee Day") }
        item { MissionMetaRow() }
        item { CoffeeMissionAbout() }
        item { CoffeeHowToCompleteCard() }
        item { CoffeeProofRequiredCard() }
        item {
            Button(
                onClick = onJoinMission,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MissionPrimary)
            ) {
                Text("JOIN MISSION", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        item { Spacer(Modifier.height(10.dp)) }
    }
}

@Composable
private fun MissionDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MissionPrimary)
        }
        Text(
            text = "Mission Details",
            color = MissionPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun MissionHero(imageRes: Int, title: String) {
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(28.dp))) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.52f))))
        )
        Text(
            title,
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 16.dp),
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MissionMetaRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MissionMetaChip("DEADLINE", "24 Oct, 2023", Icons.Default.Schedule, Modifier.weight(1f))
        MissionMetaChip("IMPACT LEVEL", "High Impact", Icons.Default.Eco, Modifier.weight(1f))
    }
}

@Composable
private fun MissionMetaChip(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0x1F006B1B))
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = MissionMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(value, color = MissionText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlasticKingProgressCard(progress: Float, current: Double, target: Double) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val percent = (safeProgress * 100).toInt()
    Surface(modifier = Modifier.fillMaxWidth().height(168.dp), shape = RoundedCornerShape(30.dp), color = MissionSoftSurface.copy(alpha = 0.68f)) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    drawArc(Color(0xFFD4DCD7), -90f, 360f, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                    drawArc(MissionPrimary, -90f, 360f * safeProgress, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percent%", color = MissionText, fontSize = 24.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${formatMissionNumber(current)} / ${formatMissionNumber(target)} KG", color = MissionMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(15.dp))
            Text("Your progress is being tracked automatically", color = MissionMuted, fontSize = 12.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PlasticKingAbout() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("About this Mission", color = MissionPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Help reduce single-use plastic by recycling clean bottles and containers through Eco-Recycle stations around campus. Every approved plastic deposit adds to your progress and keeps reusable material out of landfill.",
            color = MissionMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun CoffeeMissionAbout() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("About this Mission", color = MissionPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Reduce your environmental footprint by eliminating single-use plastics. Today, we focus on the ritual of the morning brew. Whether it is at the campus cafe or your favorite local spot, skip the disposable cup and go green.",
            color = MissionMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun PlasticKingHowToCompleteCard() {
    MissionStepsCard(
        steps = listOf(
            MissionStep(1, "Collect Clean Plastic", "Gather empty plastic bottles or containers that can be accepted by campus recycling stations."),
            MissionStep(2, "Rinse and Flatten", "Remove leftover liquid, rinse the container, and flatten it when possible to save bin space."),
            MissionStep(3, "Submit Your Deposit", "Use the recycle screen to submit your plastic deposit. Approved submissions will update this mission automatically.")
        )
    )
}

@Composable
private fun CoffeeHowToCompleteCard() {
    MissionStepsCard(
        steps = listOf(
            MissionStep(1, "Visit any Coffee Shop", "Head to your preferred barista. All local and campus cafes are participating in this tracking."),
            MissionStep(2, "Use Your Reusable Cup", "Ask the server to use your personal mug or tumbler instead of a paper cup."),
            MissionStep(3, "Upload Proof", "Take a photo of your reusable cup with the shop in the background to claim your points.")
        )
    )
}

private data class MissionStep(val number: Int, val title: String, val description: String)

@Composable
private fun MissionStepsCard(steps: List<MissionStep>) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color(0xFFE6EDE9)) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("How to complete", color = MissionText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
            steps.forEach { MissionStepRow(it) }
        }
    }
}

@Composable
private fun MissionStepRow(step: MissionStep) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color(0xFF86FA8C)) {
            Box(contentAlignment = Alignment.Center) { Text(step.number.toString(), color = MissionPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(step.title, color = MissionText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(step.description, color = MissionMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun CoffeeProofRequiredCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color(0x6643A047),
                    style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .padding(20.dp)
    ) {
        Text("Proof Required", modifier = Modifier.align(Alignment.TopStart), color = MissionText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Surface(modifier = Modifier.align(Alignment.TopEnd), shape = CircleShape, color = Color(0xFF86FAAC)) {
            Text("PHOTO ONLY", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MissionPrimary, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(54.dp), shape = CircleShape, color = MissionSoftSurface) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MissionMuted, modifier = Modifier.padding(15.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Capture your sustainable moment", color = MissionMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("Points are verified within 24 hours of submission", color = Color(0xFF8A8F8C), fontSize = 10.sp)
        }
    }
}

private val MissionBackground = Color(0xFFF5F7F5)
private val MissionPrimary = Color(0xFF006B1B)
private val MissionText = Color(0xFF2C2F2E)
private val MissionMuted = Color(0xFF686E6B)
private val MissionSoftSurface = Color(0xFFE6EDE9)

private fun plasticKingProgress(viewModel: MainViewModel): Float = (viewModel.plasticKg.toDouble() / 100.0).toFloat().coerceIn(0f, 1f)

private fun formatMissionNumber(value: Double): String = if (value == value.toLong().toDouble()) {
    value.toLong().toString()
} else {
    String.format("%.1f", value)
}
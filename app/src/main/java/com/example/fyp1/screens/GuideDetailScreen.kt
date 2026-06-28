package com.example.fyp1.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.R

private val DetailBackground = Color(0xFFF8FAF8)
private val DetailGreen = Color(0xFF006B1B)
private val DetailHeroGreen = Color(0xFF268630)
private val DetailText = Color(0xFF191C1B)
private val DetailMutedText = Color(0xFF3F4A3D)
private val DetailOutline = Color(0xFFE1E3E1)
private val DetailSoftGreen = Color(0xFFEAF7EA)

private data class MaterialDetail(
    val name: String,
    val icon: ImageVector,
    val impactLabel: String,
    val headline: String,
    val description: String,
    val heroVisual: GuideVisual,
    val steps: List<GuideStep>
)

private data class GuideStep(
    val title: String,
    val description: String,
    val visual: GuideVisual,
    val imageRes: Int? = null
)

private enum class GuideVisual {
    PlasticCup,
    PlasticWash,
    PlasticCode,
    PlasticFlatten,
    PaperStack,
    PaperSort,
    PaperFlatten,
    PaperDry,
    GlassJar,
    GlassRinse,
    GlassLid,
    GlassCare,
    MetalCan,
    MetalEmpty,
    MetalRinse,
    MetalCrush
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailScreen(navController: NavController, material: String) {
    val detail = remember(material) { materialDetails(material) }

    Scaffold(containerColor = DetailBackground) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(DetailBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { DetailRewardStyleHeader(title = "${detail.name} Guide", onBack = { navController.popBackStack() }) }

            item {
                EnvironmentalImpactCard(detail = detail)
            }

            item {
                Text(
                    text = "3 Simple Steps to Recycle Right",
                    color = DetailText,
                    fontSize = 23.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            detail.steps.forEachIndexed { index, step ->
                item {
                    RecyclingStepCard(
                        number = index + 1,
                        step = step
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRewardStyleHeader(title: String, onBack: () -> Unit) {
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
                tint = DetailGreen
            )
        }
        Text(
            text = title,
            color = DetailGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
@Composable
private fun EnvironmentalImpactCard(detail: MaterialDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DetailHeroGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = detail.impactLabel.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
            Text(
                text = detail.headline,
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = detail.description,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedGuideVisual(
                    visual = detail.heroVisual,
                    modifier = Modifier.size(112.dp),
                    onDarkBackground = true
                )
            }
        }
    }
}

@Composable
private fun RecyclingStepCard(number: Int, step: GuideStep) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(DetailGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = step.title,
                color = DetailText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = step.description,
            color = DetailMutedText,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.45f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DetailSoftGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DetailSoftGreen),
                contentAlignment = Alignment.Center
            ) {
                if (step.imageRes != null) {
                    Image(
                        painter = painterResource(id = step.imageRes),
                        contentDescription = step.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    GuideVisualIllustration(
                        visual = step.visual,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedGuideVisual(
    visual: GuideVisual,
    modifier: Modifier = Modifier,
    onDarkBackground: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "guide-visual")
    val floatOffset by transition.animateFloat(
        initialValue = -5f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guide-visual-offset"
    )

    Box(
        modifier = modifier.offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        GuideVisualIllustration(
            visual = visual,
            modifier = Modifier.fillMaxSize(),
            onDarkBackground = onDarkBackground
        )
    }
}

@Composable
private fun GuideVisualIllustration(
    visual: GuideVisual,
    modifier: Modifier = Modifier,
    onDarkBackground: Boolean = false
) {
    Canvas(modifier = modifier) {
        when (visual) {
            GuideVisual.PlasticCup -> drawPlasticCup(onDarkBackground)
            GuideVisual.PlasticWash -> drawWashBottle()
            GuideVisual.PlasticCode -> drawCodeBottle()
            GuideVisual.PlasticFlatten -> drawFlattenedBottle()
            GuideVisual.PaperStack -> drawPaperStack(onDarkBackground)
            GuideVisual.PaperSort -> drawPaperSort()
            GuideVisual.PaperFlatten -> drawPaperFlatten()
            GuideVisual.PaperDry -> drawPaperDry()
            GuideVisual.GlassJar -> drawGlassJar(onDarkBackground)
            GuideVisual.GlassRinse -> drawGlassRinse()
            GuideVisual.GlassLid -> drawGlassLid()
            GuideVisual.GlassCare -> drawGlassCare()
            GuideVisual.MetalCan -> drawMetalCan(onDarkBackground)
            GuideVisual.MetalEmpty -> drawMetalEmpty()
            GuideVisual.MetalRinse -> drawMetalRinse()
            GuideVisual.MetalCrush -> drawMetalCrush()
        }
    }
}

private fun DrawScope.drawPlasticCup(onDarkBackground: Boolean) {
    val cupColor = if (onDarkBackground) Color.White else Color(0xFFEAF6EA)
    val accent = if (onDarkBackground) DetailHeroGreen else DetailGreen
    val w = size.width
    val h = size.height
    val cup = Path().apply {
        moveTo(w * 0.26f, h * 0.20f)
        lineTo(w * 0.74f, h * 0.20f)
        lineTo(w * 0.66f, h * 0.82f)
        quadraticTo(w * 0.50f, h * 0.90f, w * 0.34f, h * 0.82f)
        close()
    }
    drawPath(cup, cupColor)
    drawPath(
        Path().apply {
            moveTo(w * 0.31f, h * 0.30f)
            cubicTo(w * 0.42f, h * 0.25f, w * 0.52f, h * 0.38f, w * 0.66f, h * 0.31f)
            lineTo(w * 0.64f, h * 0.42f)
            cubicTo(w * 0.51f, h * 0.47f, w * 0.42f, h * 0.35f, w * 0.32f, h * 0.40f)
            close()
        },
        accent.copy(alpha = 0.92f)
    )
}

private fun DrawScope.drawWashBottle() {
    drawSoftBackdrop()
    drawWaterStream()
    val w = size.width
    val h = size.height
    val bottle = Path().apply {
        moveTo(w * 0.42f, h * 0.28f)
        lineTo(w * 0.66f, h * 0.48f)
        lineTo(w * 0.53f, h * 0.78f)
        lineTo(w * 0.29f, h * 0.58f)
        close()
    }
    drawPath(bottle, Color.White.copy(alpha = 0.82f))
    drawPath(bottle, Color(0xFFB8D5C0).copy(alpha = 0.32f))
    drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.48f))
    drawCircle(Color.White.copy(alpha = 0.7f), radius = w * 0.025f, center = Offset(w * 0.58f, h * 0.62f))
}

private fun DrawScope.drawCodeBottle() {
    drawSoftBackdrop()
    drawRoundRect(Color.White.copy(alpha = 0.72f), topLeft = Offset(size.width * 0.34f, size.height * 0.16f), size = Size(size.width * 0.32f, size.height * 0.64f), cornerRadius = CornerRadius(26f, 26f))
    drawRoundRect(DetailGreen, topLeft = Offset(size.width * 0.41f, size.height * 0.42f), size = Size(size.width * 0.18f, size.height * 0.11f), cornerRadius = CornerRadius(8f, 8f))
    drawCircle(Color(0xFFA0F399), radius = size.width * 0.04f, center = Offset(size.width * 0.47f, size.height * 0.47f))
    drawCircle(Color(0xFFA0F399), radius = size.width * 0.025f, center = Offset(size.width * 0.54f, size.height * 0.47f))
}

private fun DrawScope.drawFlattenedBottle() {
    drawSoftBackdrop()
    val w = size.width
    val h = size.height
    val bottle = Path().apply {
        moveTo(w * 0.20f, h * 0.50f)
        cubicTo(w * 0.34f, h * 0.36f, w * 0.54f, h * 0.39f, w * 0.76f, h * 0.48f)
        cubicTo(w * 0.64f, h * 0.62f, w * 0.42f, h * 0.62f, w * 0.20f, h * 0.50f)
    }
    drawPath(bottle, Color.White.copy(alpha = 0.82f))
    drawRoundRect(DetailGreen.copy(alpha = 0.85f), topLeft = Offset(w * 0.60f, h * 0.43f), size = Size(w * 0.12f, h * 0.10f), cornerRadius = CornerRadius(8f, 8f))
}

private fun DrawScope.drawPaperStack(onDarkBackground: Boolean) {
    val paper = if (onDarkBackground) Color.White else Color(0xFFFFFBFE)
    val line = if (onDarkBackground) DetailHeroGreen else DetailGreen
    drawRoundRect(paper.copy(alpha = 0.95f), topLeft = Offset(size.width * 0.28f, size.height * 0.16f), size = Size(size.width * 0.44f, size.height * 0.62f), cornerRadius = CornerRadius(10f, 10f))
    drawRect(line.copy(alpha = 0.35f), topLeft = Offset(size.width * 0.36f, size.height * 0.33f), size = Size(size.width * 0.28f, size.height * 0.025f))
    drawRect(line.copy(alpha = 0.35f), topLeft = Offset(size.width * 0.36f, size.height * 0.43f), size = Size(size.width * 0.22f, size.height * 0.025f))
    drawRect(line.copy(alpha = 0.35f), topLeft = Offset(size.width * 0.36f, size.height * 0.53f), size = Size(size.width * 0.26f, size.height * 0.025f))
}

private fun DrawScope.drawPaperSort() {
    drawSoftBackdrop()
    drawPaperStack(false)
    drawCircle(DetailGreen, radius = size.width * 0.055f, center = Offset(size.width * 0.72f, size.height * 0.35f))
}

private fun DrawScope.drawPaperFlatten() {
    drawSoftBackdrop()
    drawRoundRect(Color.White, topLeft = Offset(size.width * 0.20f, size.height * 0.36f), size = Size(size.width * 0.60f, size.height * 0.30f), cornerRadius = CornerRadius(8f, 8f))
    drawRect(DetailGreen.copy(alpha = 0.25f), topLeft = Offset(size.width * 0.28f, size.height * 0.48f), size = Size(size.width * 0.44f, size.height * 0.025f))
}

private fun DrawScope.drawPaperDry() {
    drawSoftBackdrop()
    drawPaperStack(false)
    drawCircle(Color(0xFFFFD166), radius = size.width * 0.07f, center = Offset(size.width * 0.72f, size.height * 0.25f))
}

private fun DrawScope.drawGlassJar(onDarkBackground: Boolean) {
    val glass = if (onDarkBackground) Color.White.copy(alpha = 0.9f) else Color(0xFFE6F6F2)
    val line = if (onDarkBackground) DetailHeroGreen else Color(0xFF009688)
    drawRoundRect(glass, topLeft = Offset(size.width * 0.32f, size.height * 0.24f), size = Size(size.width * 0.36f, size.height * 0.58f), cornerRadius = CornerRadius(28f, 28f))
    drawRoundRect(line, topLeft = Offset(size.width * 0.38f, size.height * 0.16f), size = Size(size.width * 0.24f, size.height * 0.10f), cornerRadius = CornerRadius(10f, 10f))
    drawCircle(Color.White.copy(alpha = 0.6f), radius = size.width * 0.035f, center = Offset(size.width * 0.43f, size.height * 0.45f))
}

private fun DrawScope.drawGlassRinse() {
    drawSoftBackdrop()
    drawWaterStream()
    drawGlassJar(false)
}

private fun DrawScope.drawGlassLid() {
    drawSoftBackdrop()
    drawGlassJar(false)
    drawCircle(DetailGreen, radius = size.width * 0.06f, center = Offset(size.width * 0.69f, size.height * 0.24f))
}

private fun DrawScope.drawGlassCare() {
    drawSoftBackdrop()
    drawGlassJar(false)
    drawRoundRect(Color(0xFFFFC857), topLeft = Offset(size.width * 0.26f, size.height * 0.66f), size = Size(size.width * 0.48f, size.height * 0.09f), cornerRadius = CornerRadius(10f, 10f))
}

private fun DrawScope.drawMetalCan(onDarkBackground: Boolean) {
    val metal = if (onDarkBackground) Color.White.copy(alpha = 0.92f) else Color(0xFFE6E9E7)
    val line = if (onDarkBackground) DetailHeroGreen else Color(0xFF6F7A6B)
    drawRoundRect(metal, topLeft = Offset(size.width * 0.32f, size.height * 0.18f), size = Size(size.width * 0.36f, size.height * 0.64f), cornerRadius = CornerRadius(28f, 28f))
    drawRoundRect(line.copy(alpha = 0.55f), topLeft = Offset(size.width * 0.38f, size.height * 0.34f), size = Size(size.width * 0.24f, size.height * 0.08f), cornerRadius = CornerRadius(8f, 8f))
    drawRoundRect(line.copy(alpha = 0.35f), topLeft = Offset(size.width * 0.38f, size.height * 0.53f), size = Size(size.width * 0.24f, size.height * 0.035f), cornerRadius = CornerRadius(6f, 6f))
}

private fun DrawScope.drawMetalEmpty() {
    drawSoftBackdrop()
    drawMetalCan(false)
    drawCircle(DetailGreen, radius = size.width * 0.05f, center = Offset(size.width * 0.70f, size.height * 0.32f))
}

private fun DrawScope.drawMetalRinse() {
    drawSoftBackdrop()
    drawWaterStream()
    drawMetalCan(false)
}

private fun DrawScope.drawMetalCrush() {
    drawSoftBackdrop()
    drawRoundRect(Color(0xFFE6E9E7), topLeft = Offset(size.width * 0.25f, size.height * 0.43f), size = Size(size.width * 0.50f, size.height * 0.22f), cornerRadius = CornerRadius(18f, 18f))
    drawRect(Color(0xFF6F7A6B).copy(alpha = 0.35f), topLeft = Offset(size.width * 0.34f, size.height * 0.52f), size = Size(size.width * 0.32f, size.height * 0.03f))
}

private fun DrawScope.drawSoftBackdrop() {
    drawCircle(Color.White.copy(alpha = 0.56f), radius = size.minDimension * 0.36f, center = Offset(size.width * 0.50f, size.height * 0.50f))
    drawCircle(Color(0xFFCBE4CD).copy(alpha = 0.35f), radius = size.minDimension * 0.28f, center = Offset(size.width * 0.32f, size.height * 0.32f))
}

private fun DrawScope.drawWaterStream() {
    drawRoundRect(Color(0xFF7EC8E3).copy(alpha = 0.74f), topLeft = Offset(size.width * 0.52f, size.height * 0.05f), size = Size(size.width * 0.035f, size.height * 0.42f), cornerRadius = CornerRadius(12f, 12f))
    drawCircle(Color.White.copy(alpha = 0.75f), radius = size.width * 0.018f, center = Offset(size.width * 0.47f, size.height * 0.38f))
    drawCircle(Color.White.copy(alpha = 0.7f), radius = size.width * 0.014f, center = Offset(size.width * 0.58f, size.height * 0.45f))
}

private fun materialDetails(material: String): MaterialDetail {
    return when (material.lowercase()) {
        "paper" -> MaterialDetail(
            name = "Paper",
            icon = Icons.Filled.Description,
            impactLabel = "Environmental Impact",
            headline = "Paper can be recycled into new sheets again.",
            description = "Every clean sheet recovered reduces the need for fresh pulp and keeps campus waste lighter.",
            heroVisual = GuideVisual.PaperStack,
            steps = listOf(
                GuideStep("Keep It Dry", "Wet or oily paper can contaminate the batch. Keep paper clean, dry, and free from food residue.", GuideVisual.PaperDry, R.drawable.paper_keep_dry),
                GuideStep("Sort the Paper", "Remove plastic windows, tape, and non-paper attachments before recycling.", GuideVisual.PaperSort, R.drawable.paper_sort),
                GuideStep("Flatten & Bin", "Flatten boxes and stack papers neatly so the recycling bin has more usable space.", GuideVisual.PaperFlatten, R.drawable.paper_flatten_bin)
            )
        )
        "glass" -> MaterialDetail(
            name = "Glass",
            icon = Icons.Filled.WineBar,
            impactLabel = "Environmental Impact",
            headline = "Glass can be recycled endlessly.",
            description = "Clean bottles and jars can return as new glass without losing quality, saving raw materials and energy.",
            heroVisual = GuideVisual.GlassJar,
            steps = listOf(
                GuideStep("Rinse Clean", "Rinse bottles and jars to remove leftover drinks or food before depositing.", GuideVisual.GlassRinse, R.drawable.glass_step_1),
                GuideStep("Remove the Lid", "Separate metal or plastic caps when possible so the glass is easier to verify.", GuideVisual.GlassLid, R.drawable.glass_step_2),
                GuideStep("Handle Safely", "Do not place sharp broken glass loosely in the bin. Keep it contained for safer handling.", GuideVisual.GlassCare, R.drawable.glass_step_3)
            )
        )
        "metal" -> MaterialDetail(
            name = "Metal",
            icon = Icons.Filled.Hardware,
            impactLabel = "Environmental Impact",
            headline = "Metal recycling saves major energy.",
            description = "Recycling cans and containers uses far less energy than making new metal from raw ore.",
            heroVisual = GuideVisual.MetalCan,
            steps = listOf(
                GuideStep("Empty First", "Pour out all remaining liquid or food so the item does not contaminate other recyclables.", GuideVisual.MetalEmpty, R.drawable.metal_step_1),
                GuideStep("Rinse If Needed", "Give sticky or oily cans a quick rinse before placing them in the bin.", GuideVisual.MetalRinse, R.drawable.metal_step_2),
                GuideStep("Crush & Bin", "Crush cans when possible to save space and make collection more efficient.", GuideVisual.MetalCrush, R.drawable.metal_step_3)
            )
        )
        else -> MaterialDetail(
            name = "Plastic",
            icon = Icons.Filled.LocalDrink,
            impactLabel = "Environmental Impact",
            headline = "Plastic takes 450 years to decompose.",
            description = "Every bottle recycled is a step toward a cleaner campus and a healthier planet. Let's change the trajectory together.",
            heroVisual = GuideVisual.PlasticCup,
            steps = listOf(
                GuideStep("Clean & Dry", "Empty all liquids and rinse food containers. Residual organic matter can contaminate an entire recycling batch, turning valuable material into landfill waste.", GuideVisual.PlasticWash, R.drawable.plastic_step_1),
                GuideStep("Check the Code", "Look for the triangle symbol. On campus, we primarily accept Type 1 PET and Type 2 HDPE plastics.", GuideVisual.PlasticCode, R.drawable.plastic_step_2),
                GuideStep("Flatten & Bin", "Remove caps if needed and flatten the bottle. This saves space in the collection bins and reduces transport emissions.", GuideVisual.PlasticFlatten, R.drawable.plastic_step_3)
            )
        )
    }
}



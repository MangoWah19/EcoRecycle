package com.example.fyp1.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fyp1.MainViewModel
import com.example.fyp1.R
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.BackendMission
import com.example.fyp1.api.BackendSubmission
import com.example.fyp1.api.MissionGuideStep
import com.example.fyp1.api.MissionRepository
import com.example.fyp1.api.MissionSelectionCache
import com.example.fyp1.api.SubmitMissionRequest
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonSyntaxException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val missionGuideGson = Gson()

@Composable
fun MissionDetailsScreen(navController: NavController, viewModel: MainViewModel, missionType: String) {
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        BackendMissionDetailContent(
            padding = padding,
            missionId = missionType,
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
private fun BackendMissionDetailContent(
    padding: PaddingValues,
    missionId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val missionRepository = remember { MissionRepository(context) }
    val cachedMission = remember(missionId) {
        MissionSelectionCache.selectedMission?.takeIf { it.id == missionId }
    }
    var mission by remember(missionId) { mutableStateOf(cachedMission) }
    var submissions by remember(missionId) { mutableStateOf<List<BackendSubmission>>(emptyList()) }
    var isLoading by remember(missionId) { mutableStateOf(cachedMission == null) }
    var isJoiningMission by remember(missionId) { mutableStateOf(false) }
    var isSubmittingProof by remember(missionId) { mutableStateOf(false) }
    var errorMessage by remember(missionId) { mutableStateOf<String?>(null) }
    var actionMessage by remember(missionId) { mutableStateOf<String?>(null) }
    var explanation by remember(missionId) { mutableStateOf("") }
    var quantity by remember(missionId) { mutableStateOf("") }
    var proofPhotoUri by remember(missionId) { mutableStateOf<Uri?>(null) }
    var proofPhotoBitmap by remember(missionId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(missionId) {
        isLoading = mission == null
        errorMessage = null
        when (val result = missionRepository.getMission(missionId)) {
            is AuthResult.Success -> mission = result.value
            is AuthResult.Error -> errorMessage = result.message
        }
        when (val result = missionRepository.getMySubmissions()) {
            is AuthResult.Success -> submissions = result.value.filter { it.missionId == missionId }
            is AuthResult.Error -> if (errorMessage == null) errorMessage = result.message
        }
        isLoading = false
    }

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

        if (isLoading) {
            item { DetailInfoMessage("Loading mission details...") }
            return@LazyColumn
        }

        if (errorMessage != null && mission == null) {
            item { DetailInfoMessage(errorMessage ?: "Could not load mission details.") }
            return@LazyColumn
        }

        mission?.let { currentMission ->
            val ongoingSubmission = submissions.latestForMissionWithStatuses(missionId, setOf("ONGOING"))
            val pendingSubmission = submissions.latestForMissionWithStatuses(missionId, setOf("PENDING_REVIEW"))
            val approvedSubmissions = submissions.filter { it.status == "APPROVED" }
            val progress = missionProgress(currentMission, submissions)
            val isMissionComplete = progress.percent >= 100
            val recentSubmissions = submissions
                .filter { it.status != "ONGOING" && (it.proofImageUrl != null || it.proofText != null || it.quantity != null) }
                .sortedByDescending { it.submittedAt.orEmpty() }
            val currentSubmission = ongoingSubmission ?: pendingSubmission ?: approvedSubmissions.maxByOrNull { it.submittedAt.orEmpty() }
            val hasActiveSubmission = currentSubmission != null
            val canSubmitProof = !isMissionComplete && pendingSubmission == null && (ongoingSubmission != null || approvedSubmissions.isNotEmpty())
            val buttonText = when (currentSubmission?.status) {
                "ONGOING" -> "SUBMIT PROOF"
                "PENDING_REVIEW" -> "PENDING REVIEW"
                "APPROVED" -> if (isMissionComplete) "COMPLETED" else "SUBMIT PROOF"
                "REJECTED" -> "JOIN MISSION"
                else -> "JOIN MISSION"
            }
            val buttonEnabled = !isJoiningMission && !isSubmittingProof && currentSubmission?.status != "PENDING_REVIEW" && !isMissionComplete

            item { MissionHero(currentMission) }
            item {
                MissionMetaRow(
                    startDate = formatMissionDate(currentMission.startAt),
                    deadline = formatMissionDate(currentMission.endAt)
                )
            }
            if (hasActiveSubmission) {
                item { MissionProgressCard(progress) }
                item { RecentSubmissionsSection(recentSubmissions) }
            }
            item {
                MissionAbout(
                    text = currentMission.longDescription
                )
            }
            item { MissionStepsCard(currentMission.guideSteps()) }
            if (canSubmitProof) {
                item {
                    ProofRequiredCard(
                        explanation = explanation,
                        onExplanationChange = { explanation = it },
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        proofPhotoUri = proofPhotoUri,
                        proofPhotoBitmap = proofPhotoBitmap,
                        onProofPhotoUriChange = {
                            proofPhotoUri = it
                            if (it != null) proofPhotoBitmap = null
                        },
                        onProofPhotoBitmapChange = {
                            proofPhotoBitmap = it
                            if (it != null) proofPhotoUri = null
                        },
                        onRemoveProofPhoto = {
                            proofPhotoUri = null
                            proofPhotoBitmap = null
                        },
                        showQuantity = currentMission.type == "QUANTITY_BASED"
                    )
                }
            }
            actionMessage?.let { message ->
                item { DetailInfoMessage(message) }
            }
            if (isMissionComplete) {
                item { MissionCompletedMessage() }
            } else {
                item {
                Button(
                    onClick = {
                        actionMessage = null
                        if (canSubmitProof) {
                            isSubmittingProof = true
                            return@Button
                        }
                        isJoiningMission = true
                    },
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MissionPrimary)
                ) {
                    Text(
                        when {
                            isJoiningMission -> "JOINING..."
                            isSubmittingProof -> "SUBMITTING..."
                            else -> buttonText
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }

    LaunchedEffect(isJoiningMission) {
        if (!isJoiningMission) return@LaunchedEffect

        when (val result = missionRepository.joinMission(missionId)) {
            is AuthResult.Success -> {
                submissions = (submissions.filterNot { it.id == result.value.id } + result.value)
                actionMessage = null
            }
            is AuthResult.Error -> {
                actionMessage = result.message
            }
        }
        isJoiningMission = false
    }

    LaunchedEffect(isSubmittingProof) {
        if (!isSubmittingProof) return@LaunchedEffect

        val currentMission = mission
        if (currentMission == null) {
            actionMessage = "Mission details are still loading. Please try again."
            isSubmittingProof = false
            return@LaunchedEffect
        }

        val photo = selectedProofPhotoBytes(context, proofPhotoUri, proofPhotoBitmap)
        if (photo == null) {
            actionMessage = "Please upload or take a proof photo before submitting."
            isSubmittingProof = false
            return@LaunchedEffect
        }

        if (explanation.isBlank()) {
            actionMessage = "Please enter an explanation before submitting."
            isSubmittingProof = false
            return@LaunchedEffect
        }

        val quantityValue = quantity.toIntOrNull()
        if (currentMission.type == "QUANTITY_BASED" && (quantityValue == null || quantityValue <= 0)) {
            actionMessage = "Please enter a valid quantity before submitting."
            isSubmittingProof = false
            return@LaunchedEffect
        }

        val upload = when (val result = missionRepository.uploadMissionProof(photo.bytes, photo.mimeType, photo.fileName)) {
            is AuthResult.Success -> result.value
            is AuthResult.Error -> {
                actionMessage = result.message
                isSubmittingProof = false
                return@LaunchedEffect
            }
        }

        val request = SubmitMissionRequest(
            proofText = explanation.trim().takeIf { it.isNotBlank() },
            proofImageUrl = upload.fileUrl,
            quantity = if (currentMission.type == "QUANTITY_BASED") quantityValue else null,
            uploadId = upload.id
        )

        when (val result = missionRepository.submitMission(missionId, request)) {
            is AuthResult.Success -> {
                explanation = ""
                quantity = ""
                proofPhotoUri = null
                proofPhotoBitmap = null
                submissions = (submissions.filterNot { it.id == result.value.id } + result.value)
                when (val refresh = missionRepository.getMySubmissions()) {
                    is AuthResult.Success -> submissions = refresh.value.filter { it.missionId == missionId }
                    is AuthResult.Error -> actionMessage = refresh.message
                }
                actionMessage = "Proof submitted successfully."
            }
            is AuthResult.Error -> {
                actionMessage = result.message
            }
        }

        isSubmittingProof = false
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
private fun MissionHero(mission: BackendMission) {
    val imageRequest = rememberEcoImageRequest(mission.imageUrl)
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(28.dp))) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = mission.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF245F35), Color(0xFF008A95))))
            )
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.34f),
                modifier = Modifier.align(Alignment.Center).size(96.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.52f))))
        )
        Text(
            text = mission.title,
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 16.dp),
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MissionMetaRow(startDate: String, deadline: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MissionMetaChip("START DATE", startDate, Icons.Default.Eco, Modifier.weight(1f))
        MissionMetaChip("DEADLINE", deadline, Icons.Default.Schedule, Modifier.weight(1f))
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
private fun MissionProgressCard(mission: BackendMission, submissions: List<BackendSubmission>) {
    val progress = missionProgress(mission, submissions)
    MissionProgressCard(progress)
}

@Composable
private fun MissionProgressCard(progress: MissionProgressUi) {
    val safeProgress = progress.percent.coerceIn(0, 100)
    Surface(
        modifier = Modifier.fillMaxWidth().height(184.dp),
        shape = RoundedCornerShape(36.dp),
        color = MissionSoftSurface.copy(alpha = 0.68f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    drawArc(
                        color = Color(0xFFD8DFDB),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = MissionPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * (safeProgress / 100f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$safeProgress%", color = MissionText, fontSize = 24.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text(progress.label, color = MissionMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = progress.message,
                color = MissionMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecentSubmissionsSection(submissions: List<BackendSubmission>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Recent Submissions", color = MissionPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

        if (submissions.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5EAE6))
            ) {
                Text(
                    text = "No Submission Yet",
                    modifier = Modifier.padding(18.dp),
                    color = MissionMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        submissions.take(2).forEach { submission ->
            RecentSubmissionCard(submission)
        }

        if (submissions.size > 2) {
            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { },
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, MissionPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VIEW MORE HISTORY", color = MissionPrimary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentSubmissionCard(submission: BackendSubmission) {
    val context = LocalContext.current
    val imageUrl = normalizeLocalBlobUrl(
        url = submission.proofImageUrl ?: submission.photoUrl,
        backendBaseUrl = context.getString(R.string.backend_base_url)
    )
    val imageRequest = rememberEcoImageRequest(imageUrl)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5EAE6)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MissionSoftSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageRequest != null) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Submission proof image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(26.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = submission.id,
                        color = MissionText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    SubmissionStatusRow(submission.status)
                }
            }
            Surface(shape = RoundedCornerShape(18.dp), color = MissionSoftSurface.copy(alpha = 0.75f)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("REVIEW NOTE", color = MissionMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.7.sp)
                    Text(
                        text = submission.reviewNote?.takeIf { it.isNotBlank() } ?: "No review note yet.",
                        color = MissionMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmissionStatusRow(status: String) {
    val normalized = status.uppercase(Locale.ENGLISH)
    val color = when (normalized) {
        "APPROVED" -> MissionPrimary
        "PENDING_REVIEW" -> Color(0xFFC17800)
        "REJECTED" -> Color(0xFFB02500)
        else -> MissionMuted
    }
    val label = when (normalized) {
        "PENDING_REVIEW" -> "UNDER REVIEW"
        else -> normalized.replace('_', ' ')
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = color) {}
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MissionAbout(text: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("About this Mission", color = MissionPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = text?.takeIf { it.isNotBlank() } ?: "No long description has been added for this mission.",
            color = MissionMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun MissionStepsCard(steps: List<MissionStep>) {
    Surface(shape = RoundedCornerShape(28.dp), color = MissionSoftSurface) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("How to complete", color = MissionText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
            if (steps.isEmpty()) {
                Text(
                    text = "No guide steps have been added for this mission.",
                    color = MissionMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            } else {
                steps.forEach { MissionStepRow(it) }
            }
        }
    }
}

@Composable
private fun MissionStepRow(step: MissionStep) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color(0xFF86FA8C)) {
            Box(contentAlignment = Alignment.Center) {
                Text(step.number.toString(), color = MissionPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(step.title, color = MissionText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(step.description, color = MissionMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun ProofRequiredCard(
    explanation: String,
    onExplanationChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    proofPhotoUri: Uri?,
    proofPhotoBitmap: Bitmap?,
    onProofPhotoUriChange: (Uri?) -> Unit,
    onProofPhotoBitmapChange: (Bitmap?) -> Unit,
    onRemoveProofPhoto: () -> Unit,
    showQuantity: Boolean
) {
    val context = LocalContext.current
    var quantityFocused by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    val explanationCharacterCount = explanation.length
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onProofPhotoUriChange(uri)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            onProofPhotoBitmapChange(bitmap)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Mission Proof") },
            text = { Text("Upload an existing photo or take a new photo as proof for this mission.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Upload Photo")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Take Photo")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showQuantity) 610.dp else 500.dp)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MissionProofPhotoPicker(
                proofPhotoUri = proofPhotoUri,
                proofPhotoBitmap = proofPhotoBitmap,
                onClick = { showPhotoSourceDialog = true },
                onRemove = onRemoveProofPhoto
            )
            Spacer(Modifier.height(10.dp))
            Text("Capture your sustainable moment", color = MissionMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("JPEG or PNG, max 5MB", color = Color(0xFF8A8F8C), fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(Modifier.height(22.dp))
            MissionProofFieldLabel("Explanation")
            OutlinedTextField(
                value = explanation,
                onValueChange = { onExplanationChange(it.take(1200)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(122.dp),
                placeholder = {
                    Text("Describe your sustainable action...", color = Color(0xFF758091), fontSize = 15.sp)
                },
                textStyle = TextStyle(color = MissionText, fontSize = 15.sp),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MissionInputSurface,
                    unfocusedContainerColor = MissionInputSurface,
                    focusedBorderColor = Color(0xFF9EA8A3),
                    unfocusedBorderColor = Color(0xFFB9C2BD),
                    cursorColor = MissionPrimary
                )
            )
            Text(
                text = "$explanationCharacterCount/1200 characters",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 8.dp),
                color = MissionMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
            if (showQuantity) {
                Spacer(Modifier.height(14.dp))
                MissionProofFieldLabel("Quantity")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MissionInputSurface,
                    border = BorderStroke(1.dp, if (quantityFocused) MissionPrimary else Color(0xFFB9C2BD))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (quantity.isBlank()) {
                            Text("Enter quantity collected", color = Color(0xFF758091), fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = quantity,
                            onValueChange = { onQuantityChange(it.filter { char -> char in '0'..'9' }) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { quantityFocused = it.isFocused },
                            textStyle = TextStyle(color = MissionText, fontSize = 15.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                Text(
                    text = "Unit is based on the mission context and subject.",
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp, end = 8.dp),
                    color = MissionMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun MissionProofPhotoPicker(
    proofPhotoUri: Uri?,
    proofPhotoBitmap: Bitmap?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val hasPhoto = proofPhotoUri != null || proofPhotoBitmap != null
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (hasPhoto) 138.dp else 92.dp)
            .clip(RoundedCornerShape(18.dp))
            .drawBehind {
                drawRoundRect(
                    color = Color(0x6643A047),
                    style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)),
                    cornerRadius = CornerRadius(18.dp.toPx())
                )
            }
            .clickable(enabled = !hasPhoto) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            proofPhotoUri != null -> {
                AsyncImage(
                    model = proofPhotoUri,
                    contentDescription = "Mission proof photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            proofPhotoBitmap != null -> {
                Image(
                    bitmap = proofPhotoBitmap.asImageBitmap(),
                    contentDescription = "Mission proof photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = MissionSoftSurface) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Upload Photo", color = MissionPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (hasPhoto) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .clickable { onRemove() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove proof photo",
                    tint = MissionText,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }
    }
}

@Composable
private fun MissionProofFieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        color = MissionText,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun DetailInfoMessage(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MissionSoftSurface) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MissionMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MissionCompletedMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFE2F6E6),
        border = BorderStroke(1.dp, Color(0x33006B1B))
    ) {
        Text(
            text = "Congratulations! You have completed this mission.",
            modifier = Modifier.padding(18.dp),
            color = MissionPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

private data class MissionStep(val number: Int, val title: String, val description: String)

private data class MissionProgressUi(val percent: Int, val label: String, val message: String)

private fun BackendMission.guideSteps(): List<MissionStep> {
    return parsedGuideSteps()
        ?.sortedBy { it.step }
        ?.map { MissionStep(it.step, it.title, it.description) }
        ?.takeIf { it.isNotEmpty() }
        ?: emptyList()
}

private fun BackendMission.parsedGuideSteps(): List<MissionGuideStep>? {
    val guideJson = guide ?: return null
    return try {
        val guideArray = when {
            guideJson is JsonArray -> guideJson
            guideJson.isJsonPrimitive && guideJson.asJsonPrimitive.isString ->
                missionGuideGson.fromJson<JsonArray>(guideJson.asString, JsonArray::class.java)
            else -> null
        } ?: return null

        guideArray.mapNotNull { item ->
            val stepObject = item.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val step = stepObject.get("step")?.takeIf { it.isJsonPrimitive }?.asInt ?: return@mapNotNull null
            val title = stepObject.get("title")?.takeIf { it.isJsonPrimitive }?.asString ?: return@mapNotNull null
            val description = stepObject.get("description")?.takeIf { it.isJsonPrimitive }?.asString ?: return@mapNotNull null

            MissionGuideStep(step = step, title = title, description = description)
        }
    } catch (_: JsonSyntaxException) {
        null
    } catch (_: IllegalStateException) {
        null
    }
}

private fun List<BackendSubmission>.latestForMission(missionId: String): BackendSubmission? {
    return filter { it.missionId == missionId }
        .maxByOrNull { it.submittedAt.orEmpty() }
}

private fun List<BackendSubmission>.latestForMissionWithStatuses(
    missionId: String,
    statuses: Set<String>
): BackendSubmission? {
    return filter { it.missionId == missionId && it.status in statuses }
        .maxByOrNull { it.submittedAt.orEmpty() }
}

private fun missionProgress(mission: BackendMission, submissions: List<BackendSubmission>): MissionProgressUi {
    val approvedSubmissions = submissions.filter { it.status == "APPROVED" }
    return when (mission.type) {
        "QUANTITY_BASED" -> {
            val current = approvedSubmissions.sumOf { it.quantity ?: 0 }
            val target = mission.targetQuantity?.coerceAtLeast(1) ?: 1
            MissionProgressUi(
                percent = ((current.toFloat() / target.toFloat()) * 100).toInt().coerceIn(0, 100),
                label = "$current/$target SUBMITTED",
                message = "Your submitted quantity is being tracked automatically"
            )
        }
        "STREAK_BASED" -> {
            val current = approvedSubmissions.size
            val target = mission.targetDays?.coerceAtLeast(1) ?: 1
            MissionProgressUi(
                percent = ((current.toFloat() / target.toFloat()) * 100).toInt().coerceIn(0, 100),
                label = "$current/$target DAYS",
                message = "Your joined streak is being tracked automatically"
            )
        }
        "TIME_LIMITED" -> {
            val completed = approvedSubmissions.isNotEmpty()
            MissionProgressUi(
                percent = if (completed) 100 else 0,
                label = if (completed) "1/1 COMPLETED" else "0/1 STARTED",
                message = "Your progress is being tracked automatically"
            )
        }
        else -> MissionProgressUi(
            percent = 0,
            label = "0/1 STARTED",
            message = "Your progress is being tracked automatically"
        )
    }
}

private data class ProofPhotoPayload(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)

private fun selectedProofPhotoBytes(
    context: android.content.Context,
    uri: Uri?,
    bitmap: Bitmap?
): ProofPhotoPayload? {
    if (bitmap != null) {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        return ProofPhotoPayload(
            bytes = output.toByteArray(),
            mimeType = "image/jpeg",
            fileName = "mission-proof.jpg"
        )
    }

    if (uri != null) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return ProofPhotoPayload(
            bytes = bytes,
            mimeType = mimeType,
            fileName = "mission-proof.$extension"
        )
    }

    return null
}

private fun normalizeLocalBlobUrl(url: String?, backendBaseUrl: String): String? {
    if (url.isNullOrBlank()) return null
    val backendHost = Regex("""https?://([^/:]+)""").find(backendBaseUrl)?.groupValues?.getOrNull(1)
        ?: return url

    return url
        .replace("http://localhost:10000", "http://$backendHost:10000")
        .replace("http://127.0.0.1:10000", "http://$backendHost:10000")
        .replace("http://10.0.2.2:10000", "http://$backendHost:10000")
}

private fun formatMissionDate(value: String): String {
    return try {
        DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(value))
    } catch (_: DateTimeParseException) {
        value.takeIf { it.isNotBlank() } ?: "-"
    }
}

private val MissionBackground = Color(0xFFF5F7F5)
private val MissionPrimary = Color(0xFF006B1B)
private val MissionText = Color(0xFF2C2F2E)
private val MissionMuted = Color(0xFF686E6B)
private val MissionSoftSurface = Color(0xFFE6EDE9)
private val MissionInputSurface = Color(0xFFF2F5F3)

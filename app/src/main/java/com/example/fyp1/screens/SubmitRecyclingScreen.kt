package com.example.fyp1.screens

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.fyp1.ui.theme.FYP1Theme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.fyp1.*
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.RecyclingRepository
import com.example.fyp1.components.*
import com.example.fyp1.offline.ConnectionModeChip
import com.example.fyp1.offline.ConnectionUiMode
import com.example.fyp1.offline.rememberConnectionUiMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitRecyclingScreen(navController: NavController, viewModel: MainViewModel) {
    var weight by remember { mutableStateOf("") }
    var selectedMat by remember { mutableStateOf("Plastic") }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var proofPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var proofPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showProofPhotoRequired by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var popOutMessage by remember { mutableStateOf<AppPopOutMessage?>(null) }
    var pointRates by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var ratesLoaded by remember { mutableStateOf(false) }
    var ratesLoadFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val connectionMode = rememberConnectionUiMode()
    val isOffline = connectionMode == ConnectionUiMode.Offline
    val hasProofPhoto = proofPhotoUri != null || proofPhotoBitmap != null
    val showPopOut = { title: String, message: String, type: PopOutMessageType ->
        popOutMessage = AppPopOutMessage(title = title, message = message, type = type)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            proofPhotoUri = uri
            proofPhotoBitmap = null
            showProofPhotoRequired = false
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            proofPhotoBitmap = bitmap
            proofPhotoUri = null
            showProofPhotoRequired = false
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            showPopOut("Camera access needed", "Please allow camera permission to take a proof photo for your deposit.", PopOutMessageType.Error)
        }
    }
    LaunchedEffect(Unit) {
        when (val result = RecyclingRepository(context).getPointRates()) {
            is AuthResult.Success -> {
                pointRates = result.value
                ratesLoaded = true
                ratesLoadFailed = result.value.isEmpty()
            }
            is AuthResult.Error -> {
                pointRates = emptyMap()
                ratesLoaded = true
                ratesLoadFailed = true
            }
        }
    }

    val submitDeposit: () -> Unit = submit@{
        val w = weight.toDoubleOrNull()
        when {
            weight.isBlank() -> {
                showPopOut("Wait a second!", "Please enter a valid weight for your deposit before submitting.", PopOutMessageType.Error)
            }
            w == null -> {
                showPopOut("Wait a second!", "Please enter a numeric weight such as 1.5 kg before submitting.", PopOutMessageType.Error)
            }
            w <= 0 -> {
                showPopOut("Wait a second!", "Weight must be greater than 0 kg before submitting.", PopOutMessageType.Error)
            }
            else -> {
                if (!hasProofPhoto) {
                    showProofPhotoRequired = true
                    showPopOut("Photo required", "Please upload or take a proof photo before submitting your deposit.", PopOutMessageType.Error)
                    return@submit
                }

                val maxKgByMaterial = mapOf(
                    "Metal" to 40.0,
                    "Plastic" to 30.0,
                    "Glass" to 20.0,
                    "Paper" to 50.0
                )
                val materialCap = maxKgByMaterial[selectedMat] ?: 50.0
                if (w > materialCap) {
                    showPopOut("Weight too high", "Maximum single submission for $selectedMat is ${materialCap.toInt()} kg.", PopOutMessageType.Error)
                } else {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val submitted = viewModel.submitRecyclingLog(
                                selectedMat,
                                w,
                                context,
                                proofPhotoUri,
                                proofPhotoBitmap
                            )
                            if (submitted) {
                                weight = ""
                                proofPhotoUri = null
                                proofPhotoBitmap = null
                                showProofPhotoRequired = false
                            }
                        } catch (e: Exception) {
                            showPopOut("Submission failed", "An unexpected error occurred. Please try again. (${e.localizedMessage})", PopOutMessageType.Error)
                        } finally {
                    isSubmitting = false
                        }
                    }
                }
            }
        }
    }


    AppPopOutDialog(
        message = popOutMessage,
        onDismiss = { popOutMessage = null }
    )

    if (showPhotoSourceDialog) {
        AppPopOutDialog(
            message = AppPopOutMessage(
                title = "Proof of Deposit",
                message = "Upload an existing photo or take a new photo before submitting your deposit.",
                type = PopOutMessageType.Info,
                buttonText = "Upload Photo",
                secondaryButtonText = "Take Photo"
            ),
            onDismiss = { showPhotoSourceDialog = false },
            onPrimary = {
                showPhotoSourceDialog = false
                galleryLauncher.launch("image/*")
            },
            onSecondary = {
                showPhotoSourceDialog = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
    EcoNavigationDrawer(navController = navController) { openDrawer ->
    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SubmitTopBar(
                    connectionMode = connectionMode,
                    onMenuClick = openDrawer,
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            item {
                DepositMethodIntroCard(
                    onInfoClick = {
                        popOutMessage = AppPopOutMessage(
                            title = "Deposit Methods",
                            message = "Use QR Scan when an admin or station provides a QR code. Use Manual Submission when you need to enter the material, weight, and proof photo yourself. Both methods need internet so your record can be checked and saved.",
                            type = PopOutMessageType.Info
                        )
                    }
                )
            }

            item {
                DepositMethodLabel(
                    title = "Method 1: QR Code Deposit",
                    subtitle = "Fastest option when a station or admin gives you a valid QR code."
                )
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    ScanStationCard(
                        enabled = !isOffline,
                        onClick = { navController.navigate("qr_scanner") }
                    )
                    if (isOffline) {
                        OfflineDepositMask(
                            message = "QR deposit needs internet.",
                            modifier = Modifier.matchParentSize(),
                            onClick = {
                                popOutMessage = offlineDepositMessage()
                            }
                        )
                    }
                }
            }

            item {
                DepositMethodLabel(
                    title = "Method 2: Manual Submission",
                    subtitle = "Enter your material, weight, and proof photo for admin review."
                )
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubmitDepositFormCard(
                        selectedMat = selectedMat,
                        expanded = expanded,
                        weight = weight,
                        isSubmitting = isSubmitting,
                        proofPhotoUri = proofPhotoUri,
                        proofPhotoBitmap = proofPhotoBitmap,
                        showProofPhotoError = showProofPhotoRequired,
                        pointRates = pointRates,
                        ratesLoaded = ratesLoaded,
                        ratesLoadFailed = ratesLoadFailed,
                        onExpandedChange = { expanded = it },
                        onMaterialSelected = { selectedMat = it; expanded = false },
                        onProofPhotoClick = { showPhotoSourceDialog = true },
                        onProofPhotoRemove = {
                            proofPhotoUri = null
                            proofPhotoBitmap = null
                            showProofPhotoRequired = false
                        },
                        onWeightChange = { input ->
                            if (input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                weight = input
                            }
                        },
                        onSubmit = submitDeposit
                    )
                    if (isOffline) {
                        OfflineDepositMask(
                            message = "Manual submission needs internet.",
                            modifier = Modifier.matchParentSize(),
                            onClick = {
                                popOutMessage = offlineDepositMessage()
                            }
                        )
                    }
                }
            }

            item {
                SubmissionNoteBox()
            }

            item {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
    }
}

@Composable
private fun SubmitTopBar(connectionMode: ConnectionUiMode, onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF006B1B))
            }
            Text(
                text = "Eco-Recycle",
                color = Color(0xFF006B1B),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionModeChip(connectionMode)
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = Color(0xFFE6E9E7),
                border = BorderStroke(2.dp, Color(0x1A006B1B))
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF006B1B),
                    modifier = Modifier.padding(9.dp)
                )
            }
        }
    }
}

private fun offlineDepositMessage() = AppPopOutMessage(
    title = "Internet Required",
    message = "Please reconnect to the internet before making a recycling deposit. You can still view cached pages while offline.",
    type = PopOutMessageType.Info
)

@Composable
private fun DepositMethodIntroCard(onInfoClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color(0xFFE6F6E9)
            ) {
                Icon(
                    imageVector = Icons.Default.Recycling,
                    contentDescription = null,
                    tint = Color(0xFF006B1B),
                    modifier = Modifier.padding(11.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Choose a Deposit Method",
                    color = Color(0xFF1A1C1A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Scan a QR code for station deposits, or submit the details manually with proof.",
                    color = Color(0xFF6D7772),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            IconButton(onClick = onInfoClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Deposit method information",
                    tint = Color(0xFF006B1B)
                )
            }
        }
    }
}

@Composable
private fun DepositMethodLabel(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF006B1B),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = subtitle,
            color = Color(0xFF6D7772),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun OfflineDepositMask(message: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.84f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xBFF5F7F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color(0xFF6E7772),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "OFFLINE MODE",
                    color = Color(0xFF3C4540),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = message,
                    color = Color(0xFF6E7772),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScanStationCard(enabled: Boolean = true, onClick: () -> Unit) {
    val borderColor = Color(0x5543A047)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                )
            }
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(82.dp),
                shape = CircleShape,
                color = Color(0x1243A047),
                border = BorderStroke(8.dp, Color(0x0A43A047))
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color(0xFF1A1C1A),
                    modifier = Modifier.padding(23.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Scan Station QR Code",
                color = Color(0xFF1A1C1A),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "To start your recycling deposit",
                color = Color(0xFF595C5B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitDepositFormCard(
    selectedMat: String,
    expanded: Boolean,
    weight: String,
    isSubmitting: Boolean,
    proofPhotoUri: Uri?,
    proofPhotoBitmap: Bitmap?,
    showProofPhotoError: Boolean,
    pointRates: Map<String, Int>,
    ratesLoaded: Boolean,
    ratesLoadFailed: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMaterialSelected: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onProofPhotoClick: () -> Unit,
    onProofPhotoRemove: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFE8EBE9))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Column {
                FieldLabel("Material Type")
                Spacer(Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
                    OutlinedTextField(
                        value = selectedMat,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        shape = CircleShape,
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                        val materialNames = (pointRates.keys + POINT_RATES.keys).sorted()
                        materialNames.forEach { material ->
                            val rate = pointRates[material]
                            val rateText = when {
                                rate != null -> "$rate pts/kg"
                                !ratesLoaded -> "loading rate"
                                else -> "rate unavailable"
                            }
                            DropdownMenuItem(
                                text = { Text("$material ($rateText)") },
                                onClick = { onMaterialSelected(material) }
                            )
                        }
                    }
                }
                if (ratesLoadFailed) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Point rates could not be loaded from the backend. Please check with admin before submitting.",
                        color = Color(0xFFE67E22),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Column {
                FieldLabel("Weight (kg)")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.0", color = Color(0x66747776)) },
                    trailingIcon = {
                        Text("kg", color = Color(0xFF43A047), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    },
                    enabled = !isSubmitting,
                    singleLine = true,
                    shape = CircleShape
                )
            }

            Column {
                FieldLabel("Proof of Deposit")
                Spacer(Modifier.height(10.dp))
                ProofPhotoPicker(
                    proofPhotoUri = proofPhotoUri,
                    proofPhotoBitmap = proofPhotoBitmap,
                    showError = showProofPhotoError,
                    enabled = !isSubmitting,
                    onClick = onProofPhotoClick,
                    onRemove = onProofPhotoRemove
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                enabled = !isSubmitting,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit Deposit", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProofPhotoPicker(
    proofPhotoUri: Uri?,
    proofPhotoBitmap: Bitmap?,
    showError: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val hasPhoto = proofPhotoUri != null || proofPhotoBitmap != null
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (hasPhoto) 150.dp else 82.dp)
                .clip(RoundedCornerShape(18.dp))
                .drawBehind {
                    drawRoundRect(
                        color = Color(0x6643A047),
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
                    )
                }
                .clickable(enabled = enabled && !hasPhoto) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                proofPhotoUri != null -> {
                    AsyncImage(
                        model = proofPhotoUri,
                        contentDescription = "Proof of deposit photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                proofPhotoBitmap != null -> {
                    Image(
                        bitmap = proofPhotoBitmap.asImageBitmap(),
                        contentDescription = "Proof of deposit photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Upload Photo",
                            color = Color(0xFF43A047),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (hasPhoto) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clickable(enabled = enabled) { onRemove() },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove proof photo",
                        tint = Color(0xFF1D1F1D),
                        modifier = Modifier.padding(7.dp)
                    )
                }
            }
        }
        if (showError) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "PHOTO REQUIRED",
                    color = Color(0xFFE53935),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFF595C5B),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.3.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SubmissionNoteBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x0D43A047), RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF43A047),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Note: Submissions will be reviewed within 24 working hours (excluding weekends and public holidays). Points will be credited once verified by the campus team.",
            color = Color(0xFF595C5B),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}











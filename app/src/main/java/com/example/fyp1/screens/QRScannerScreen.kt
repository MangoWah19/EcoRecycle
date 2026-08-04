package com.example.fyp1.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.api.AuthResult
import com.example.fyp1.components.AppPopOutDialog
import com.example.fyp1.components.AppPopOutMessage
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.components.PopOutMessageType
import com.google.gson.JsonParser
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isProcessingGallery by remember { mutableStateOf(false) }
    var isClaimingQr by remember { mutableStateOf(false) }
    var scanPaused by remember { mutableStateOf(false) }
    var exitAfterDialog by remember { mutableStateOf(false) }
    var popOutMessage by remember { mutableStateOf<AppPopOutMessage?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            popOutMessage = AppPopOutMessage(
                title = "Camera Permission Needed",
                message = "Please allow camera access so EcoRecycle can scan QR codes.",
                type = PopOutMessageType.Info
            )
        }
    }
    val handleScanResult: (String) -> Unit = { value ->
        if (scanPaused || isClaimingQr) {
            Unit
        } else if (!isEcoRecycleRecyclingQr(value)) {
            scanPaused = true
            popOutMessage = AppPopOutMessage(
                title = "Invalid QR code",
                message = "This QR code is not an EcoRecycle recycling deposit QR. Please scan the QR generated from the admin page.",
                type = PopOutMessageType.Error
            )
        } else {
            scanPaused = true
            isClaimingQr = true
            popOutMessage = AppPopOutMessage(
                title = "Checking QR",
                message = "Please wait while the backend verifies this recycling deposit QR.",
                type = PopOutMessageType.Info
            )
            scope.launch {
                when (val result = viewModel.claimRecyclingQrPayload(value, context)) {
                    is AuthResult.Success -> {
                        val log = result.value
                        exitAfterDialog = true
                        popOutMessage = AppPopOutMessage(
                            title = "QR deposit submitted",
                            message = "${log.quantity} kg of ${log.material_type} is now pending admin review. Points will be awarded once approved.",
                            type = PopOutMessageType.Success,
                            buttonText = "Back to Submit"
                        )
                    }
                    is AuthResult.Error -> {
                        exitAfterDialog = false
                        popOutMessage = AppPopOutMessage(
                            title = qrErrorTitle(result.message),
                            message = result.message,
                            type = PopOutMessageType.Error,
                            buttonText = "Scan Again"
                        )
                    }
                }
                isClaimingQr = false
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isProcessingGallery = true
            scanQrFromGallery(
                uri = uri,
                context = context,
                scanner = scanner,
                onScanned = { value ->
                    isProcessingGallery = false
                    handleScanResult(value)
                },
                onNotFound = {
                    isProcessingGallery = false
                    scanPaused = true
                    popOutMessage = AppPopOutMessage(
                        title = "No valid QR found",
                        message = "This image does not contain an EcoRecycle recycling QR. Please choose the QR generated from the admin page.",
                        type = PopOutMessageType.Error,
                        buttonText = "Scan Again"
                    )
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
        }
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                QRScannerCameraPreview(
                    scanner = scanner,
                    scanPaused = scanPaused || isClaimingQr || popOutMessage != null,
                    onQrScanned = handleScanResult,
                    onCameraError = {
                        popOutMessage = AppPopOutMessage(
                            title = "Camera Unavailable",
                            message = "We could not open the camera. Please check camera permission or try again.",
                            type = PopOutMessageType.Error
                        )
                    }
                )
            } else {
                PermissionPrompt(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            ScannerOverlay(
                isProcessingGallery = isProcessingGallery || isClaimingQr,
                onPickGallery = { galleryLauncher.launch("image/*") },
                onBack = { navController.popBackStack() }
            )

            AppPopOutDialog(
                message = popOutMessage,
                onDismiss = {
                    popOutMessage = null
                    if (exitAfterDialog) {
                        exitAfterDialog = false
                        navController.popBackStack()
                    } else {
                        scanPaused = false
                    }
                }
            )
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalGetImage::class)
@Composable
private fun QRScannerCameraPreview(
    scanner: BarcodeScanner,
    scanPaused: Boolean,
    onQrScanned: (String) -> Unit,
    onCameraError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { imageAnalysis ->
                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            processQrImage(
                                imageProxy = imageProxy,
                                scanner = scanner,
                                alreadyScanned = scanPaused,
                                onScanned = { value ->
                                    onQrScanned(value)
                                }
                            )
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    onCameraError()
                }
            }, ContextCompat.getMainExecutor(viewContext))
            previewView
        }
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            onClick = {
                torchOn = !torchOn
                camera?.cameraControl?.enableTorch(torchOn)
            },
            modifier = Modifier.padding(bottom = 116.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.78f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = null,
                    tint = Color(0xFF2C2F2E),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Flashlight",
                    color = Color(0xFF2C2F2E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@ExperimentalGetImage
private fun processQrImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    alreadyScanned: Boolean,
    onScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null || alreadyScanned) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val value = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
            if (!value.isNullOrBlank()) {
                onScanned(value)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun scanQrFromGallery(
    uri: Uri,
    context: android.content.Context,
    scanner: BarcodeScanner,
    onScanned: (String) -> Unit,
    onNotFound: () -> Unit
) {
    val image = runCatching { InputImage.fromFilePath(context, uri) }.getOrNull()
    if (image == null) {
        onNotFound()
        return
    }

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val value = barcodes
                .firstOrNull { it.format == Barcode.FORMAT_QR_CODE && !it.rawValue.isNullOrBlank() }
                ?.rawValue
            if (!value.isNullOrBlank() && isEcoRecycleRecyclingQr(value)) {
                onScanned(value)
            } else {
                onNotFound()
            }
        }
        .addOnFailureListener {
            onNotFound()
        }
}

private fun isEcoRecycleRecyclingQr(value: String): Boolean {
    return runCatching {
        val root = JsonParser.parseString(value).asJsonObject
        val payload = root.getAsJsonObject("payload") ?: return false
        val signature = root.get("signature")?.asString
        payload.get("type")?.asString == "recycling-deposit" &&
            !payload.get("qrId")?.asString.isNullOrBlank() &&
            !payload.get("nonce")?.asString.isNullOrBlank() &&
            !payload.get("materialType")?.asString.isNullOrBlank() &&
            payload.get("estimatedWeightKg") != null &&
            !payload.get("expiresAt")?.asString.isNullOrBlank() &&
            !signature.isNullOrBlank()
    }.getOrDefault(false)
}

private fun qrErrorTitle(message: String): String {
    val lower = message.lowercase()
    return when {
        "expired" in lower -> "QR expired"
        "already used" in lower || "invalid" in lower || "replay" in lower -> "QR cannot be used"
        "permission" in lower || "log in" in lower -> "Login required"
        else -> "QR could not be used"
    }
}

@Composable
private fun ScannerOverlay(
    isProcessingGallery: Boolean,
    onPickGallery: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.78f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF006B1B)
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.82f)
            ) {
                Text(
                    text = "Eco-Recycle Scan",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = Color(0xFF2C2F2E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(44.dp))
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QRFrame(modifier = Modifier.size(240.dp))
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Align QR code within frame",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Point your camera at the code on the smart bin to start depositing.",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 46.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = onPickGallery,
            enabled = !isProcessingGallery,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 176.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.88f))
        ) {
            if (isProcessingGallery) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF006B1B),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color(0xFF006B1B),
                modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = if (isProcessingGallery) "Checking Image" else "Scan from Gallery",
                color = Color(0xFF006B1B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QRFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        val color = Color(0xFF8DED89)
        val corner = 34.dp.toPx()
        val line = 54.dp.toPx()
        val inset = 4.dp.toPx()
        val width = size.width
        val height = size.height

        drawArc(color, 180f, 90f, false, Offset(inset, inset), Size(corner * 2, corner * 2), style = stroke)
        drawLine(color, Offset(inset + corner, inset), Offset(inset + corner + line, inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(inset, inset + corner), Offset(inset, inset + corner + line), strokeWidth = stroke.width, cap = StrokeCap.Round)

        drawArc(color, 270f, 90f, false, Offset(width - inset - corner * 2, inset), Size(corner * 2, corner * 2), style = stroke)
        drawLine(color, Offset(width - inset - corner, inset), Offset(width - inset - corner - line, inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(width - inset, inset + corner), Offset(width - inset, inset + corner + line), strokeWidth = stroke.width, cap = StrokeCap.Round)

        drawArc(color, 90f, 90f, false, Offset(inset, height - inset - corner * 2), Size(corner * 2, corner * 2), style = stroke)
        drawLine(color, Offset(inset + corner, height - inset), Offset(inset + corner + line, height - inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(inset, height - inset - corner), Offset(inset, height - inset - corner - line), strokeWidth = stroke.width, cap = StrokeCap.Round)

        drawArc(color, 0f, 90f, false, Offset(width - inset - corner * 2, height - inset - corner * 2), Size(corner * 2, corner * 2), style = stroke)
        drawLine(color, Offset(width - inset - corner, height - inset), Offset(width - inset - corner - line, height - inset), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(width - inset, height - inset - corner), Offset(width - inset, height - inset - corner - line), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera permission needed",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Allow camera access to scan the smart bin QR code.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )
            androidx.compose.material3.Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B1B))
            ) {
                Text("Allow Camera")
            }
        }
    }
}


package com.example.fyp1.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                QRScannerCameraPreview(navController = navController)
            } else {
                PermissionPrompt(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            ScannerOverlay(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalGetImage::class)
@Composable
private fun QRScannerCameraPreview(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
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
                                alreadyScanned = scanned,
                                onScanned = { value ->
                                    scanned = true
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("qr_scan_result", value)
                                    Toast.makeText(context, "Scanned: $value", Toast.LENGTH_SHORT).show()
                                    // TODO: Use scanned station identifier when deposit flow supports it.
                                    navController.popBackStack()
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
                    Toast.makeText(context, "Unable to open camera.", Toast.LENGTH_LONG).show()
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

@Composable
private fun ScannerOverlay(onBack: () -> Unit) {
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


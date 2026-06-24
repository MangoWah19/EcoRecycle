package com.example.fyp1.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.material.icons.filled.WineBar
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.fyp1.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitRecyclingScreen(navController: NavController, viewModel: MainViewModel) {
    var weight by remember { mutableStateOf("") }
    var selectedMat by remember { mutableStateOf("Plastic") }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scannedQrResult = backStackEntry?.savedStateHandle?.get<String>("qr_scan_result")

    LaunchedEffect(scannedQrResult) {
        scannedQrResult?.let { value ->
            Toast.makeText(context, "Scanned station: $value", Toast.LENGTH_SHORT).show()
            // TODO: Store scanned station identifier when deposit submission supports it.
            backStackEntry?.savedStateHandle?.remove<String>("qr_scan_result")
        }
    }

    val submitDeposit: () -> Unit = {
        val w = weight.toDoubleOrNull()
        when {
            weight.isBlank() -> {
                Toast.makeText(context, "Weight Required: Please enter the estimated weight in kg before submitting.", Toast.LENGTH_SHORT).show()
            }
            w == null -> {
                Toast.makeText(context, "Invalid Weight: \"$weight\" is not a valid number. Please enter a numeric value (e.g. 1.5).", Toast.LENGTH_SHORT).show()
            }
            w <= 0 -> {
                Toast.makeText(context, "Invalid Weight: Weight must be greater than 0 kg. Please enter the correct amount.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val maxKgByMaterial = mapOf(
                    "Metal" to 40.0,
                    "Plastic" to 30.0,
                    "Glass" to 20.0,
                    "Paper" to 50.0
                )
                val materialCap = maxKgByMaterial[selectedMat] ?: 50.0
                if (w > materialCap) {
                    Toast.makeText(
                        context,
                        "Weight Too High: Maximum single submission for $selectedMat is ${materialCap.toInt()} kg. ",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    isSubmitting = true
                    scope.launch {
                        try {
                            viewModel.submitRecyclingLog(selectedMat, w, context)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Submission Failed: An unexpected error occurred. Please try again. (${e.localizedMessage})",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            }
        }
    }

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7F5))
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SubmitTopBar(onProfileClick = { navController.navigate("profile") })
            }

            item {
                ScanStationCard(onClick = { navController.navigate("qr_scanner") })
            }

            item {
                SubmitDepositFormCard(
                    selectedMat = selectedMat,
                    expanded = expanded,
                    weight = weight,
                    isSubmitting = isSubmitting,
                    onExpandedChange = { expanded = it },
                    onMaterialSelected = { selectedMat = it; expanded = false },
                    onWeightChange = { input ->
                        if (input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weight = input
                        }
                    },
                    onSubmit = submitDeposit
                )
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

@Composable
private fun SubmitTopBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF006B1B))
            }
            Text(
                text = "Eco-Recycle",
                color = Color(0xFF006B1B),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
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

@Composable
private fun ScanStationCard(onClick: () -> Unit) {
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
            .clickable { onClick() },
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
    onExpandedChange: (Boolean) -> Unit,
    onMaterialSelected: (String) -> Unit,
    onWeightChange: (String) -> Unit,
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
                        POINT_RATES.forEach { (material, rate) ->
                            DropdownMenuItem(
                                text = { Text("$material ($rate pts/kg)") },
                                onClick = { onMaterialSelected(material) }
                            )
                        }
                    }
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
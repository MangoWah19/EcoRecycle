package com.example.fyp1.navigation

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
import androidx.compose.material.icons.filled.Person
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
import com.example.fyp1.screens.*

@Composable
fun AppNavigation(activity: ComponentActivity, initialIntent: Intent?) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    var currentIntent by remember { mutableStateOf(initialIntent) }

    LaunchedEffect(currentIntent) {
        if (isResetIntent(currentIntent)) {
            navController.navigate("reset_password") {
                popUpTo("login") { inclusive = false }
            }
            currentIntent = Intent()
        }
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> { newIntent ->
            currentIntent = newIntent
        }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("home") { HomeScreen(navController, viewModel) }
        composable("rewards") { RewardsScreen(navController, viewModel) }
        composable("profile") { ProfileScreen(navController, viewModel) }
        composable("submit_recycling") { SubmitRecyclingScreen(navController, viewModel) }
        composable("recycling_history") { RecyclingHistoryScreen(navController, viewModel) }
        composable("qr_scanner") { QRScannerScreen(navController) }
        composable("eco_learning") { EcoLearningScreen(navController) }
        composable("recycling_guide") { RecyclingGuideScreen(navController) }
        composable("guide_detail/{material}") { backStack ->
            val mat = backStack.arguments?.getString("material") ?: ""
            GuideDetailScreen(navController, mat)
        }
        composable("edit_profile") { EditProfileScreen(navController, viewModel) }
        composable("leaderboard") { LeaderboardScreen(navController, viewModel) }
        composable("missions") { MissionsScreen(navController, viewModel) }
        composable("mission_details/{missionType}") { backStack ->
            MissionDetailsScreen(navController, viewModel, backStack.arguments?.getString("missionType") ?: "zero_waste_coffee")
        }
        composable("achievements") { AchievementsScreen(navController, viewModel) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable("reset_password") { ResetPasswordScreen(navController) }
    }
}

private fun isResetIntent(intent: Intent?): Boolean {
    val data = intent?.data ?: return false
    if (data.scheme != "com.example.fyp1" || data.host != "reset-callback") return false
    val accessToken = data.getQueryParameter("access_token")
    val type = data.getQueryParameter("type")
    return type == "recovery" && !accessToken.isNullOrBlank()
}


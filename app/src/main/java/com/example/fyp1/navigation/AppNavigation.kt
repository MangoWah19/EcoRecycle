package com.example.fyp1.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.NotificationRepository
import com.example.fyp1.components.AppPopOutDialog
import com.example.fyp1.components.InAppNotificationBanner
import com.example.fyp1.components.PopOutMessageType
import com.example.fyp1.offline.LocalNotificationEntity
import com.example.fyp1.offline.isNetworkAvailable
import com.example.fyp1.screens.*

@Composable
fun AppNavigation(activity: ComponentActivity, initialIntent: Intent?) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val notificationRepository = remember { NotificationRepository(context) }

    var currentIntent by remember { mutableStateOf(initialIntent) }
    var inAppNotification by remember { mutableStateOf<LocalNotificationEntity?>(null) }
    var latestNotificationCreatedAt by remember { mutableStateOf<Long?>(null) }
    val notificationQueue = remember { mutableStateListOf<LocalNotificationEntity>() }
    val queuedNotificationIds = remember { mutableSetOf<String>() }

    LaunchedEffect(notificationRepository) {
        notificationRepository.observeNotifications().collect { notifications ->
            val sortedNotifications = notifications.sortedBy { it.createdAt }
            val newest = sortedNotifications.lastOrNull() ?: return@collect
            val baseline = latestNotificationCreatedAt
            if (baseline == null) {
                latestNotificationCreatedAt = newest.createdAt
                return@collect
            }

            val newBannerNotifications = sortedNotifications
                .filter { it.createdAt > baseline }
                .filter { it.shouldShowInAppBanner() }
                .filter { queuedNotificationIds.add(it.id) }

            if (newest.createdAt > baseline) {
                latestNotificationCreatedAt = newest.createdAt
            }

            notificationQueue.addAll(newBannerNotifications)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (inAppNotification != null || notificationQueue.isEmpty()) {
                delay(120)
                continue
            }
            val nextNotification = notificationQueue.removeAt(0)
            vibrateForEcoUpdate(context)
            inAppNotification = nextNotification
            delay(3000)
            if (inAppNotification?.id == nextNotification.id) {
                inAppNotification = null
                queuedNotificationIds.remove(nextNotification.id)
                delay(260)
            }
        }
    }

    LaunchedEffect(currentIntent) {
        if (isResetIntent(currentIntent)) {
            navController.navigate("reset_password") {
                popUpTo("auth_loading") { inclusive = false }
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

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "auth_loading") {
            composable("auth_loading") { AuthLoadingScreen(navController, viewModel) }
            composable("login") { LoginScreen(navController, viewModel) }
            composable("home") { HomeScreen(navController, viewModel) }
            composable("rewards") { RewardsScreen(navController, viewModel) }
            composable("profile") { ProfileScreen(navController, viewModel) }
            composable("notifications") { NotificationScreen(navController) }
            composable("point_transactions") { PointLedgerScreen(navController) }
            composable("saved_content") { SavedContentScreen(navController) }
            composable("submit_recycling") { SubmitRecyclingScreen(navController, viewModel) }
            composable("recycling_history") { RecyclingHistoryScreen(navController, viewModel) }
            composable("qr_scanner") { QRScannerScreen(navController, viewModel) }
            composable("eco_learning") { EcoLearningScreen(navController) }
            composable("content_detail/{contentId}") { backStack ->
                ContentDetailScreen(navController, backStack.arguments?.getString("contentId") ?: "")
            }
            composable("quiz_attempt/{contentId}") { backStack ->
                QuizAttemptScreen(navController, backStack.arguments?.getString("contentId") ?: "")
            }
            composable("quiz_review") { QuizReviewScreen(navController) }
            composable("about_app") { AboutAppScreen(navController) }
            composable("how_it_works") { HowItWorksScreen(navController) }
            composable("sustainability_policy") { SustainabilityPolicyScreen(navController) }
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

        InAppNotificationBanner(
            notification = inAppNotification,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    AppPopOutDialog(
        message = viewModel.popOutMessage,
        onDismiss = { viewModel.dismissPopOut() }
    )
}

private fun LocalNotificationEntity.shouldShowInAppBanner(): Boolean =
    title.contains("Approved", ignoreCase = true) ||
        title.contains("Redeemed", ignoreCase = true) ||
        title.contains("Points", ignoreCase = true) ||
        title.contains("Badge", ignoreCase = true) ||
        (category.equals("REWARD", ignoreCase = true) && sourceId?.startsWith("BDG") == true) ||
        title.contains("Rank", ignoreCase = true)

private fun vibrateForEcoUpdate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(70L)
    }
}

@Composable
private fun AuthLoadingScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    var loadingMessage by remember { mutableStateOf("Checking your saved login...") }

    LaunchedEffect(Unit) {
        val startedAt = System.currentTimeMillis()
        suspend fun waitForMinimumLoadingTime() {
            val remainingDelay = 1500L - (System.currentTimeMillis() - startedAt)
            if (remainingDelay > 0) delay(remainingDelay)
        }

        val savedUser = authRepository.getSavedUser()
        if (savedUser == null || !authRepository.isLoggedIn()) {
            loadingMessage = "Please log in to continue."
            waitForMinimumLoadingTime()
            navController.navigate("login") {
                popUpTo("auth_loading") { inclusive = true }
            }
            return@LaunchedEffect
        }

        viewModel.applyBackendUser(savedUser)
        if (!context.isNetworkAvailable()) {
            loadingMessage = "Offline mode. Opening cached content for ${savedUser.name}..."
            waitForMinimumLoadingTime()
            navController.navigate("home") {
                popUpTo("auth_loading") { inclusive = true }
            }
            return@LaunchedEffect
        }

        loadingMessage = "Welcome back, ${savedUser.name}. Verifying your session..."
        when (val result = authRepository.me()) {
            is AuthResult.Success -> {
                viewModel.applyBackendUser(result.value)
                loadingMessage = "Login verified. Opening your dashboard..."
                waitForMinimumLoadingTime()
                navController.navigate("home") {
                    popUpTo("auth_loading") { inclusive = true }
                }
            }
            is AuthResult.Error -> {
                if (result.message.startsWith("Connection Error")) {
                    loadingMessage = "Offline mode. Opening cached content for ${savedUser.name}..."
                    viewModel.showPopOut(
                        title = "Offline Mode",
                        message = "You're offline, so we opened your saved EcoRecycle session. Some features will update when you're online again.",
                        type = PopOutMessageType.Info
                    )
                    waitForMinimumLoadingTime()
                    navController.navigate("home") {
                        popUpTo("auth_loading") { inclusive = true }
                    }
                } else {
                    authRepository.clearSession()
                    viewModel.clearBackendUser()
                    loadingMessage = "Your session has expired. Please log in again."
                    viewModel.showPopOut(
                        title = "Login Required",
                        message = result.message,
                        type = PopOutMessageType.Error
                    )
                    waitForMinimumLoadingTime()
                    navController.navigate("login") {
                        popUpTo("auth_loading") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F5))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App logo",
                modifier = Modifier.size(92.dp)
            )
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color(0xFFE6EDE9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFF006B1B),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = loadingMessage,
                color = Color(0xFF2C2F2E),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Please wait while we prepare your account.",
                color = Color(0xFF686E6B),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun isResetIntent(intent: Intent?): Boolean {
    val data = intent?.data ?: return false
    if (data.scheme != "com.example.fyp1" || data.host != "reset-callback") return false
    val accessToken = data.getQueryParameter("access_token")
    val type = data.getQueryParameter("type")
    return type == "recovery" && !accessToken.isNullOrBlank()
}


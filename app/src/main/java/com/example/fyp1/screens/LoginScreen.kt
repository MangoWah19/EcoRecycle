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
import com.example.fyp1.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Inline validation states 闂?only shown after user attempts to submit
    val passwordError = if (hasAttemptedSubmit) {
        when {
            password.isBlank() -> "Password is required."
            else -> null
        }
    } else null

    val emailError = if (hasAttemptedSubmit) {
        when {
            email.isBlank() -> "Email is required."
            !email.contains("@") || !email.contains(".") -> "Enter a valid email (e.g. user@example.com)."
            !email.endsWith("@student.uow.edu.my") -> "Please enter your student account"
            else -> null
        }
    } else null

    val fullNameError = if (hasAttemptedSubmit && isSignUp) {
        when {
            fullName.isBlank() -> "Full name is required."
            else -> null
        }
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FCF9))
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Eco,
            null,
            tint = Color(0xFF1DB954),
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Eco-Recycle",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color(0xFF1B5E20)
        )
        Spacer(Modifier.height(32.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = fullNameError != null,
                supportingText = fullNameError?.let { { Text(it, color = Color(0xFFB00020)) } }
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it, color = Color(0xFFB00020)) } }
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = Color(0xFFB00020)) } }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // Mark that user has attempted to submit 闂?this triggers inline field errors
                hasAttemptedSubmit = true

// Extra domain check as a second safety net for sign up
                if (isSignUp && !email.endsWith("@student.uow.edu.my")) {
                    Toast.makeText(
                        context,
                        "Please enter your uow student account .",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                val hasFieldErrors = emailError != null || passwordError != null || fullNameError != null
                if (hasFieldErrors) return@Button

                isLoading = true
                scope.launch {
                    try {
                        if (isSignUp) {
                            supabase.auth.signUpWith(Email) {
                                this.email = email
                                this.password = password
                                data = buildJsonObject {
                                    put("full_name", fullName)
                                }
                            }

                            delay(1500)

                            val newUser = supabase.auth.currentUserOrNull()
                            newUser?.let { user ->
                                try {
                                    supabase.postgrest["profiles"].insert(
                                        Profile(
                                            id = user.id,
                                            username = email.substringBefore("@"),
                                            full_name = fullName,
                                            total_points = 0,
                                            lifetime_points = 0
                                        )
                                    )
                                } catch (e: Exception) {
                                    // Profile might already exist
                                }
                            }

                            isLoading = false
                            Toast.makeText(
                                context,
                                "Registration Successful! Your account has been created. You can now log in.",
                                Toast.LENGTH_LONG
                            ).show()

                            email = ""
                            password = ""
                            fullName = ""
                            hasAttemptedSubmit = false
                            isSignUp = false

                        } else {
                            supabase.auth.signInWith(Email) {
                                this.email = email
                                this.password = password
                            }

                            isLoading = false

                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
                        isLoading = false

                        val errorMessage = when {
                            e.message?.contains("Invalid login credentials") == true ->
                                "Login Failed: The email or password you entered is incorrect. Please double-check and try again."
                            e.message?.contains("User already registered") == true ->
                                "Account Already Exists: This email address is already registered. Please use the Login option instead."
                            e.message?.contains("Password should be at least") == true ->
                                "Weak Password: Your password must be at least 6 characters long. Please choose a stronger password."
                            e.message?.contains("Unable to validate email address") == true ->
                                "Invalid Email: The email address format is not valid. Please check and try again."
                            e.message?.contains("Network") == true || e.message?.contains("timeout") == true ->
                                "Connection Error: Unable to reach the server. Please check your internet connection and try again."
                            else -> "Unexpected Error: Something went wrong. Please try again. (${e.localizedMessage ?: "Unknown error"})"
                        }

                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = if (isSignUp) "Register" else "Login")
            }
        }

        TextButton(
            onClick = {
                if (!isLoading) {
                    email = ""
                    password = ""
                    fullName = ""
                    hasAttemptedSubmit = false
                    isSignUp = !isSignUp
                }
            },
            enabled = !isLoading
        ) {
            Text(text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Register")
        }
        TextButton(
            onClick = { navController.navigate("forgot_password") },
            enabled = !isLoading
        ) {
            Text("Forgot Password?", color = Color(0xFF1DB954))
        }
    }

}


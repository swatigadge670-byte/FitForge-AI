package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FitForgeApp(viewModel: FitForgeViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("dashboard") } // dashboard, workouts, nutrition, tracking, ai, settings
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        bottomBar = {
            if (viewModel.isLoggedIn) {
                FitForgeBottomBar(currentTab = currentTab, onTabSelected = { currentTab = it })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!viewModel.isLoggedIn) {
                AuthFlowScreen(viewModel = viewModel)
            } else {
                Crossfade(targetState = currentTab, label = "ScreenTransition") { tab ->
                    when (tab) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel, onNavigateToWorkouts = { currentTab = "workouts" })
                        "workouts" -> WorkoutsScreen(viewModel = viewModel)
                        "nutrition" -> NutritionScreen(viewModel = viewModel)
                        "tracking" -> TrackingScreen(viewModel = viewModel)
                        "ai" -> AICoachScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
            
            // Render active workout overlay if any workout is active
            viewModel.activeWorkoutPlan?.let { activePlan ->
                ActiveWorkoutOverlay(viewModel = viewModel, plan = activePlan)
            }
        }
    }
}

@Composable
fun FitForgeBottomBar(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = FitForgeSurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("fitforge_bottom_bar")
    ) {
        val items = listOf(
            NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard),
            NavigationItem("workouts", "Workouts", Icons.Default.FitnessCenter),
            NavigationItem("nutrition", "Nutrition", Icons.Default.Restaurant),
            NavigationItem("tracking", "Progress", Icons.Default.Timeline),
            NavigationItem("ai", "AI Coach", Icons.Default.SmartToy),
            NavigationItem("settings", "Settings", Icons.Default.Settings)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentTab == item.id,
                onClick = { onTabSelected(item.id) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitForgeSecondaryDark,
                    selectedTextColor = FitForgeSecondaryDark,
                    indicatorColor = FitForgeCardDark.copy(alpha = 0.5f),
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                ),
                modifier = Modifier.testTag("nav_${item.id}")
            )
        }
    }
}

data class NavigationItem(val id: String, val label: String, val icon: ImageVector)

// --- Auth Screens ---
@Composable
fun AuthFlowScreen(viewModel: FitForgeViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FitForgeBackgroundDark, FitForgeSurfaceDark)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // App Logo Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(FitForgeSecondaryDark, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Logo",
                    tint = FitForgeSecondaryDark,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "FITFORGE AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                "FORGE YOUR ULTIMATE PHYSIQUE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitForgeSecondaryDark,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            when (viewModel.authMode) {
                "welcome" -> WelcomeScreen(viewModel)
                "login" -> LoginScreen(viewModel)
                "register" -> RegisterScreen(viewModel)
                "forgot" -> ForgotPasswordScreen(viewModel)
            }
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: FitForgeViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Empowered by Next-Gen AI training, custom macros nutrition, and interactive biometric tracking.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = { viewModel.authMode = "login" },
            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("welcome_login_btn")
        ) {
            Text("SIGN IN WITH EMAIL", fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { viewModel.authMode = "register" },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FitForgeSecondaryDark),
            border = BorderStroke(1.5.dp, FitForgeSecondaryDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("welcome_register_btn")
        ) {
            Text("CREATE AN ACCOUNT", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = FitForgeCardDark)
            Text("  OR CONTINUE WITH  ", color = TextGray, fontSize = 11.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = FitForgeCardDark)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        IconButton(
            onClick = { viewModel.handleGoogleSignIn() },
            modifier = Modifier
                .size(60.dp)
                .background(FitForgeCardDark, CircleShape)
                .testTag("google_signin_btn")
        ) {
            Icon(
                imageVector = Icons.Default.CloudCircle,
                contentDescription = "Google",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        
        if (viewModel.isAuthLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = FitForgeSecondaryDark)
        }
    }
}

@Composable
fun LoginScreen(viewModel: FitForgeViewModel) {
    Column {
        Text("Sign In", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = viewModel.authEmail,
            onValueChange = { viewModel.authEmail = it },
            label = { Text("Email Address") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = viewModel.authPassword,
            onValueChange = { viewModel.authPassword = it },
            label = { Text("Password") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = { viewModel.authMode = "forgot" },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = FitForgeSecondaryDark, fontSize = 13.sp)
        }
        
        if (viewModel.authError.isNotEmpty()) {
            Text(
                viewModel.authError,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.handleLogin() },
            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_login_btn")
        ) {
            if (viewModel.isAuthLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("SIGN IN", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", color = TextGray)
            TextButton(onClick = { viewModel.authMode = "register" }) {
                Text("Sign Up", color = FitForgeSecondaryDark, fontWeight = FontWeight.Bold)
            }
        }
        
        TextButton(
            onClick = { viewModel.authMode = "welcome" },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back", color = TextGray)
        }
    }
}

@Composable
fun RegisterScreen(viewModel: FitForgeViewModel) {
    Column {
        Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = viewModel.authName,
            onValueChange = { viewModel.authName = it },
            label = { Text("Full Name") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = viewModel.authEmail,
            onValueChange = { viewModel.authEmail = it },
            label = { Text("Email Address") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = viewModel.authPassword,
            onValueChange = { viewModel.authPassword = it },
            label = { Text("Password") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        if (viewModel.authError.isNotEmpty()) {
            Text(
                viewModel.authError,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.handleRegister() },
            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_register_btn")
        ) {
            if (viewModel.isAuthLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("GET STARTED", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?", color = TextGray)
            TextButton(onClick = { viewModel.authMode = "login" }) {
                Text("Sign In", color = FitForgeSecondaryDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: FitForgeViewModel) {
    Column {
        Text("Reset Password", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter your email address and we'll send you link to reset your password.",
            fontSize = 14.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = viewModel.authEmail,
            onValueChange = { viewModel.authEmail = it },
            label = { Text("Email Address") },
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        if (viewModel.authError.isNotEmpty()) {
            Text(
                viewModel.authError,
                color = FitForgeSecondaryDark,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.handleForgotPassword() },
            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_forgot_btn")
        ) {
            if (viewModel.isAuthLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("SEND PASSWORD LINK", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { viewModel.authMode = "login" },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Sign In", color = TextGray)
        }
    }
}

// --- Home Dashboard ---
@Composable
fun DashboardScreen(viewModel: FitForgeViewModel, onNavigateToWorkouts: () -> Unit) {
    val activity by viewModel.todayActivity.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    val steps = activity?.steps ?: 0
    val water = activity?.waterIntakeMl ?: 0
    val calories = activity?.caloriesBurned ?: 0
    val weight = activity?.weightKg ?: profile?.weightKg ?: 70f
    
    val stepsGoal = activity?.stepTarget ?: 10000
    val waterGoal = activity?.waterTargetMl ?: 2000
    
    val displayWeight = if (viewModel.isMetric) "${weight} kg" else "${(weight * 2.20462f).toInt()} lbs"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header with dynamic premium avatar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "FITFORGE AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitForgeSecondaryDark,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Hello, ${profile?.name ?: "Fit Forger"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                
                // Elegant premium avatar icon
                val initials = (profile?.name ?: "MA")
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 2.dp,
                            color = FitForgePrimaryDark.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (initials.isNotEmpty()) initials else "FF",
                        color = FitForgeSecondaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        
        // Monetization / Standard User Support Banner
        if (viewModel.showAdBanner) {
            item {
                PremiumUpsellCard(onUpgrade = { viewModel.upgradeSubscription() })
            }
        }

        // Daily workout highlight hero card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_workout_card"),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2563EB), // Blue-600
                                    Color(0xFF1E40AF)  // Blue-800
                                )
                            )
                        )
                        .clickable { onNavigateToWorkouts() }
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "AI PICK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                "High Intensity",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Push Day Alpha",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                        Text(
                            "Target: Chest, Shoulders, Triceps • 60 mins",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onNavigateToWorkouts() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "START WORKOUT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Standard Metrics Dashboard Widgets
        item {
            Text(
                "TODAY'S PROGRESS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FitForgeSecondaryDark,
                letterSpacing = 1.sp
            )
        }

        // Steps and water gauges grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Steps Progress Gauge
                DashboardGaugeCard(
                    modifier = Modifier.weight(1f),
                    title = "Steps",
                    value = steps.toString(),
                    goal = "$stepsGoal",
                    progress = (steps.toFloat() / stepsGoal).coerceIn(0f, 1f),
                    icon = Icons.Default.DirectionsRun,
                    accentColor = FitForgeSecondaryDark,
                    onAddClick = { viewModel.addStepLog(1000) },
                    addLabel = "+1,000 steps"
                )

                // Water Intake Gauge
                DashboardGaugeCard(
                    modifier = Modifier.weight(1f),
                    title = "Water Intake",
                    value = "$water ml",
                    goal = "$waterGoal ml",
                    progress = (water.toFloat() / waterGoal).coerceIn(0f, 1f),
                    icon = Icons.Default.LocalDrink,
                    accentColor = AccentBlue,
                    onAddClick = { viewModel.addWaterLog(250) },
                    addLabel = "+250 ml"
                )
            }
        }

        // Calories & Weight widgets row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories burned widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = FitForgeCardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Calories", color = TextGray, fontSize = 12.sp)
                        }
                        Text("$calories kcal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Est. total burn", fontSize = 11.sp, color = TextGray)
                    }
                }

                // Weight Widget with inline quick input
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = FitForgeCardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    var isEditingWeight by remember { mutableStateOf(false) }
                    var weightInput by remember { mutableStateOf(weight.toString()) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Weight", color = TextGray, fontSize = 12.sp)
                            }
                            Icon(
                                imageVector = if (isEditingWeight) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Weight",
                                tint = FitForgeSecondaryDark,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        if (isEditingWeight) {
                                            val wVal = weightInput.toFloatOrNull() ?: weight
                                            viewModel.logUserWeight(wVal)
                                            isEditingWeight = false
                                        } else {
                                            isEditingWeight = true
                                        }
                                    }
                            )
                        }
                        
                        if (isEditingWeight) {
                            BasicTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                            )
                        } else {
                            Text(displayWeight, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        Text("Today's value", fontSize = 11.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardGaugeCard(
    modifier: Modifier,
    title: String,
    value: String,
    goal: String,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    onAddClick: () -> Unit,
    addLabel: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = FitForgeCardDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Custom circular progress gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value.split(" ")[0], fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Goal $goal", fontSize = 9.sp, color = TextGray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f), contentColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Text(addLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PremiumUpsellCard(onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FitForgePrimaryDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "UNLOCK FITFORGE ELITE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    "Remove Ads, unlimited AI Coach chat, advanced biometric tracking.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = FitForgePrimaryDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("upgrade_button")
            ) {
                Text("UPGRADE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// --- Workouts List Screen ---
@Composable
fun WorkoutsScreen(viewModel: FitForgeViewModel) {
    var selectedPlanForDetail by remember { mutableStateOf<WorkoutPlan?>(null) }
    var selectedTab by remember { mutableStateOf("All") } // All, Gym, Home
    
    val filteredPlans = remember(selectedTab) {
        if (selectedTab == "All") WorkoutPlans.plans
        else WorkoutPlans.plans.filter { it.type == selectedTab }
    }

    if (selectedPlanForDetail != null) {
        WorkoutDetailView(
            plan = selectedPlanForDetail!!,
            onBack = { selectedPlanForDetail = null },
            onStartWorkout = { plan ->
                viewModel.startWorkout(plan)
                selectedPlanForDetail = null
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FitForgeBackgroundDark)
                .padding(16.dp)
        ) {
            Text(
                "FORGE WORKOUT PLANS",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Choose an elite routine matched for your level and objective.",
                fontSize = 13.sp,
                color = TextGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // TabRow selector for Gym vs Home Workout plans
            TabRow(
                selectedTabIndex = if (selectedTab == "All") 0 else if (selectedTab == "Gym") 1 else 2,
                containerColor = Color.Transparent,
                contentColor = FitForgeSecondaryDark,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "All") 0 else if (selectedTab == "Gym") 1 else 2]),
                        color = FitForgeSecondaryDark
                    )
                }
            ) {
                Tab(selected = selectedTab == "All", onClick = { selectedTab = "All" }) {
                    Text("All Plans", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "All") Color.White else TextGray)
                }
                Tab(selected = selectedTab == "Gym", onClick = { selectedTab = "Gym" }) {
                    Text("Gym Plans", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Gym") Color.White else TextGray)
                }
                Tab(selected = selectedTab == "Home", onClick = { selectedTab = "Home" }) {
                    Text("Home / No Eq.", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Home") Color.White else TextGray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPlans) { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedPlanForDetail = plan }
                            .testTag("workout_plan_card_${plan.id}"),
                        colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Badges
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BadgeText(text = plan.level, containerColor = FitForgePrimaryDark.copy(alpha = 0.2f), contentColor = FitForgeSecondaryDark)
                                    BadgeText(text = plan.split, containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                                }
                                
                                Text("${plan.durationMinutes} mins", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(plan.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(plan.description, fontSize = 13.sp, color = TextGray, maxLines = 2)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${plan.exercises.size} Custom Exercises", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark)
                                Icon(Icons.Default.ArrowForward, contentDescription = "View Details", tint = FitForgeSecondaryDark, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeText(text: String, containerColor: Color, contentColor: Color) {
    Text(
        text = text.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        color = contentColor,
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// --- Workout Details and Interactive Play View ---
@Composable
fun WorkoutDetailView(plan: WorkoutPlan, onBack: () -> Unit, onStartWorkout: (WorkoutPlan) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Plan Detail", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(plan.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(plan.description, fontSize = 13.sp, color = TextGray)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BadgeText(text = "Goal: ${plan.goal}", containerColor = FitForgeCardDark, contentColor = Color.White)
            BadgeText(text = "Level: ${plan.level}", containerColor = FitForgeCardDark, contentColor = FitForgeSecondaryDark)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("EXERCISES INCLUDED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plan.exercises) { ex ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ex.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${ex.defaultSets} sets x ${ex.defaultReps} reps", fontSize = 12.sp, color = FitForgeSecondaryDark, fontWeight = FontWeight.Bold)
                        }
                        Text(ex.instructions, fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(vertical = 4.dp))
                        
                        // Video Tutorial Link
                        Text(
                            "Watch Video Tutorial",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ex.videoUrl))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onStartWorkout(plan) },
            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_workout_session_btn")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("START ACTIVE WORKOUT", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// --- Active Workout Overlay Player (Supports sets completions tracking and actual rest countdown timer) ---
@Composable
fun ActiveWorkoutOverlay(viewModel: FitForgeViewModel, plan: WorkoutPlan) {
    val currentExercise = plan.exercises[viewModel.currentExerciseIndex]
    val completedSetsCount = viewModel.completedSets.value[viewModel.currentExerciseIndex] ?: 0
    val totalSetsNeeded = currentExercise.defaultSets

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark.copy(alpha = 0.98f))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE SESSION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                    Text(plan.title, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                
                Button(
                    onClick = { viewModel.quitWorkout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Quit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Exercise Title and Progress
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "EXERCISE ${viewModel.currentExerciseIndex + 1} OF ${plan.exercises.size}",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    currentExercise.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Target: ${currentExercise.defaultSets} Sets x ${currentExercise.defaultReps} Reps",
                    fontSize = 14.sp,
                    color = FitForgeSecondaryDark,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central Rest Timer / Counter Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isRestTimerRunning) {
                    // Rest Timer display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REST TIMER", fontSize = 12.sp, color = FitForgeSecondaryDark, fontWeight = FontWeight.Bold)
                        Text(
                            "${viewModel.activeRestTimeLeft}s",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Button(
                            onClick = { viewModel.skipRestTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text("Skip", color = Color.White)
                        }
                    }
                } else {
                    // Interactive Check counter
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COMPLETED SETS", fontSize = 12.sp, color = TextGray)
                        Text(
                            "$completedSetsCount / $totalSetsNeeded",
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (completedSetsCount == totalSetsNeeded) AccentGreen else Color.White
                        )
                        Text("Tap 'LOG SET' after completing one set", fontSize = 11.sp, color = TextGray)
                    }
                }
            }

            // Actions Block
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Log Set Button
                if (!viewModel.isRestTimerRunning && completedSetsCount < totalSetsNeeded) {
                    Button(
                        onClick = { viewModel.completeExerciseSet() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("log_set_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOG COMPLETED SET", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (completedSetsCount == totalSetsNeeded) {
                    Text("All sets logged successfully for this exercise!", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Navigation controls for exercises
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.previousExercise() },
                        enabled = viewModel.currentExerciseIndex > 0,
                        modifier = Modifier
                            .background(FitForgeCardDark, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = if (viewModel.currentExerciseIndex > 0) Color.White else TextGray)
                    }

                    if (viewModel.currentExerciseIndex == plan.exercises.size - 1) {
                        Button(
                            onClick = { viewModel.finishWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = FitForgeSecondaryDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .testTag("finish_workout_session_btn")
                        ) {
                            Text("FINISH WORKOUT", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextExercise() },
                            colors = ButtonDefaults.buttonColors(containerColor = FitForgePrimaryDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("NEXT EXERCISE", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nextExercise() },
                        enabled = viewModel.currentExerciseIndex < plan.exercises.size - 1,
                        modifier = Modifier
                            .background(FitForgeCardDark, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = if (viewModel.currentExerciseIndex < plan.exercises.size - 1) Color.White else TextGray)
                    }
                }
            }
        }
    }
}

// --- Nutrition and Calculators Screen ---
@Composable
fun NutritionScreen(viewModel: FitForgeViewModel) {
    var selectedTab by remember { mutableStateOf("Calculators") } // Calculators, Meal Plan, Water

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp)
    ) {
        Text(
            "FITFORGE NUTRITION",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            "Advanced micro and calorie calculations for performance forging.",
            fontSize = 13.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TabRow(
            selectedTabIndex = if (selectedTab == "Calculators") 0 else if (selectedTab == "Meal Plan") 1 else 2,
            containerColor = Color.Transparent,
            contentColor = FitForgeSecondaryDark,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "Calculators") 0 else if (selectedTab == "Meal Plan") 1 else 2]),
                    color = FitForgeSecondaryDark
                )
            }
        ) {
            Tab(selected = selectedTab == "Calculators", onClick = { selectedTab = "Calculators" }) {
                Text("Calculators", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Calculators") Color.White else TextGray)
            }
            Tab(selected = selectedTab == "Meal Plan", onClick = { selectedTab = "Meal Plan" }) {
                Text("Diet Plans", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Meal Plan") Color.White else TextGray)
            }
            Tab(selected = selectedTab == "Water", onClick = { selectedTab = "Water" }) {
                Text("Water & Alarm", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Water") Color.White else TextGray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            "Calculators" -> CalculatorsTab(viewModel)
            "Meal Plan" -> DietPlansTab(viewModel)
            "Water" -> WaterReminderTab(viewModel)
        }
    }
}

@Composable
fun CalculatorsTab(viewModel: FitForgeViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ENTER BIOMETRIC INFO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.calcHeight,
                        onValueChange = { viewModel.calcHeight = it },
                        label = { Text("Height (cm)") },
                        textStyle = TextStyle(color = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("calc_height_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = viewModel.calcWeight,
                        onValueChange = { viewModel.calcWeight = it },
                        label = { Text("Weight (kg)") },
                        textStyle = TextStyle(color = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("calc_weight_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.calcAge,
                        onValueChange = { viewModel.calcAge = it },
                        label = { Text("Age (yrs)") },
                        textStyle = TextStyle(color = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("calc_age_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    // Gender selection
                    var genderExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = viewModel.calcGender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            textStyle = TextStyle(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) }
                        )
                        DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            DropdownMenuItem(text = { Text("Male") }, onClick = { viewModel.calcGender = "Male"; genderExpanded = false })
                            DropdownMenuItem(text = { Text("Female") }, onClick = { viewModel.calcGender = "Female"; genderExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Goal selection
                    var goalExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = viewModel.calcGoal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fitness Goal") },
                            textStyle = TextStyle(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { goalExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) }
                        )
                        DropdownMenu(expanded = goalExpanded, onDismissRequest = { goalExpanded = false }) {
                            DropdownMenuItem(text = { Text("Muscle Gain") }, onClick = { viewModel.calcGoal = "Muscle Gain"; goalExpanded = false })
                            DropdownMenuItem(text = { Text("Fat Loss") }, onClick = { viewModel.calcGoal = "Fat Loss"; goalExpanded = false })
                            DropdownMenuItem(text = { Text("Strength Training") }, onClick = { viewModel.calcGoal = "Strength Training"; goalExpanded = false })
                        }
                    }
                    
                    // Activity selector
                    var actExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = viewModel.calcActivityLevel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Activity Level") },
                            textStyle = TextStyle(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { actExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) }
                        )
                        DropdownMenu(expanded = actExpanded, onDismissRequest = { actExpanded = false }) {
                            DropdownMenuItem(text = { Text("Sedentary") }, onClick = { viewModel.calcActivityLevel = "Sedentary"; actExpanded = false })
                            DropdownMenuItem(text = { Text("Lightly Active") }, onClick = { viewModel.calcActivityLevel = "Lightly Active"; actExpanded = false })
                            DropdownMenuItem(text = { Text("Moderately Active") }, onClick = { viewModel.calcActivityLevel = "Moderately Active"; actExpanded = false })
                            DropdownMenuItem(text = { Text("Very Active") }, onClick = { viewModel.calcActivityLevel = "Very Active"; actExpanded = false })
                        }
                    }
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.calculateAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FitForgeSecondaryDark, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("recalculate_macros_btn")
                ) {
                    Text("CALCULATE BIOMETRICS", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Output Display Box
        Card(
            colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("YOUR BIOMETRIC RESULTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BMI Index", fontSize = 12.sp, color = TextGray)
                        Text(String.format("%.1f", viewModel.calcBmi), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(viewModel.calcBmiCategory, fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Target Calories", fontSize = 12.sp, color = TextGray)
                        Text("${viewModel.calcCalories} kcal", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Daily target", fontSize = 11.sp, color = TextGray)
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                Text("MACRONUTRIENT SPLIT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                // Macros Bar Graph representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    val total = viewModel.calcProtein + viewModel.calcCarbs + viewModel.calcFats
                    if (total > 0) {
                        Box(modifier = Modifier.weight(viewModel.calcProtein.toFloat()).fillMaxHeight().background(FitForgePrimaryDark))
                        Box(modifier = Modifier.weight(viewModel.calcCarbs.toFloat()).fillMaxHeight().background(AccentOrange))
                        Box(modifier = Modifier.weight(viewModel.calcFats.toFloat()).fillMaxHeight().background(AccentTeal))
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroLegend(title = "Protein", value = "${viewModel.calcProtein}g", color = FitForgePrimaryDark)
                    MacroLegend(title = "Carbs", value = "${viewModel.calcCarbs}g", color = AccentOrange)
                    MacroLegend(title = "Fats", value = "${viewModel.calcFats}g", color = AccentTeal)
                }
            }
        }
    }
}

@Composable
fun MacroLegend(title: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(title, fontSize = 11.sp, color = TextGray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun DietPlansTab(viewModel: FitForgeViewModel) {
    val diets = listOf(
        DietItem("Bulking Shred Plan", "High carb, high protein plan designed strictly for rapid muscle synthesis and mass generation.", "3,200 kcal", "220g", "350g", "90g"),
        DietItem("Keto Fat Torch", "Ultra-low carbohydrate, high healthy fat scheme to induce ketosis and burn belly fat instantly.", "1,800 kcal", "140g", "20g", "130g"),
        DietItem("Lean Muscle Definition", "Moderate intake focusing on extreme lean definition, calorie balance, and strength preserving.", "2,200 kcal", "180g", "180g", "60g")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(diets) { diet ->
            Card(
                colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(diet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text(diet.desc, fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(vertical = 6.dp))
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cal: ${diet.cal}", fontSize = 11.sp, color = FitForgeSecondaryDark, fontWeight = FontWeight.Bold)
                        Text("Pro: ${diet.pro}", fontSize = 11.sp, color = Color.White)
                        Text("Carb: ${diet.carb}", fontSize = 11.sp, color = Color.White)
                        Text("Fat: ${diet.fat}", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

data class DietItem(val name: String, val desc: String, val cal: String, val pro: String, val carb: String, val fat: String)

@Composable
fun WaterReminderTab(viewModel: FitForgeViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("NOTIFICATIONS & REMINDERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hourly Water Reminder", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Trigger alarms every hour to drink 250ml", fontSize = 11.sp, color = TextGray)
                }
                Switch(
                    checked = viewModel.enableWaterReminder,
                    onCheckedChange = { viewModel.enableWaterReminder = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FitForgeSecondaryDark)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Meal Planning Notification", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Trigger meal notifications for macro tracking", fontSize = 11.sp, color = TextGray)
                }
                Switch(
                    checked = viewModel.enableMealReminder,
                    onCheckedChange = { viewModel.enableMealReminder = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FitForgeSecondaryDark)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Workout Prompts", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Reminder at 8:00 AM to forge your muscles", fontSize = 11.sp, color = TextGray)
                }
                Switch(
                    checked = viewModel.enableWorkoutReminder,
                    onCheckedChange = { viewModel.enableWorkoutReminder = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FitForgeSecondaryDark)
                )
            }
        }
    }
}

// --- Biometric Progress & Tracking Screen ---
@Composable
fun TrackingScreen(viewModel: FitForgeViewModel) {
    var selectedTab by remember { mutableStateOf("Weight Logs") } // Weight Logs, History, Achievements
    val weightLogs by viewModel.weightHistory.collectAsState()
    val historyLogs by viewModel.workoutHistory.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp)
    ) {
        Text(
            "PROGRESS FORGING TRACKER",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            "Detailed historical charts, badges, and workout history logs.",
            fontSize = 13.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TabRow(
            selectedTabIndex = if (selectedTab == "Weight Logs") 0 else if (selectedTab == "History") 1 else 2,
            containerColor = Color.Transparent,
            contentColor = FitForgeSecondaryDark,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "Weight Logs") 0 else if (selectedTab == "History") 1 else 2]),
                    color = FitForgeSecondaryDark
                )
            }
        ) {
            Tab(selected = selectedTab == "Weight Logs", onClick = { selectedTab = "Weight Logs" }) {
                Text("Weight Log", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Weight Logs") Color.White else TextGray)
            }
            Tab(selected = selectedTab == "History", onClick = { selectedTab = "History" }) {
                Text("Workout History", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "History") Color.White else TextGray)
            }
            Tab(selected = selectedTab == "Achievements", onClick = { selectedTab = "Achievements" }) {
                Text("Badges", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == "Achievements") Color.White else TextGray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            "Weight Logs" -> WeightLogTab(viewModel, weightLogs)
            "History" -> WorkoutHistoryTab(historyLogs)
            "Achievements" -> AchievementsTab(profile)
        }
    }
}

@Composable
fun WeightLogTab(viewModel: FitForgeViewModel, logs: List<WeightLog>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // High fidelity custom Canvas drawing line chart for weight
        Card(
            colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("WEIGHT HISTORIC CHART", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (logs.size >= 2) {
                    val weights = logs.map { it.weightKg }
                    val minWeight = weights.minOrNull() ?: 60f
                    val maxWeight = weights.maxOrNull() ?: 80f
                    val delta = (maxWeight - minWeight).coerceAtLeast(1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val spacing = w / (logs.size - 1)
                            
                            // Draw chart grid lines
                            for (gridLine in 0..4) {
                                val gridY = h * (gridLine / 4f)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, gridY),
                                    end = Offset(w, gridY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // Map coordinate points
                            val points = logs.mapIndexed { index, log ->
                                val x = index * spacing
                                val normY = (log.weightKg - minWeight) / delta
                                val y = h - (normY * h * 0.8f) - (h * 0.1f) // 10% margins top/bottom
                                Offset(x, y)
                            }
                            
                            // Draw connecting paths
                            for (p in 0 until points.size - 1) {
                                drawLine(
                                    color = FitForgeSecondaryDark,
                                    start = points[p],
                                    end = points[p + 1],
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            
                            // Draw point circles
                            points.forEach { pt ->
                                drawCircle(
                                    color = FitForgePrimaryDark,
                                    center = pt,
                                    radius = 5.dp.toPx()
                                )
                                drawCircle(
                                    color = Color.White,
                                    center = pt,
                                    radius = 2.dp.toPx()
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add weight logs daily to render charts.", color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        }
        
        // Logs list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(logs.reversed()) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(log.date, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        val displayW = if (viewModel.isMetric) "${String.format("%.1f", log.weightKg)} kg" else "${String.format("%.1f", log.weightKg * 2.20462f)} lbs"
                        Text(displayW, color = FitForgeSecondaryDark, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryTab(logs: List<WorkoutHistory>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No workout completions recorded.", color = TextGray)
                Text("Complete a workout from Workouts tab to log details.", fontSize = 11.sp, color = TextGray)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(log.date, fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            BadgeText(text = log.type, containerColor = FitForgePrimaryDark.copy(alpha = 0.2f), contentColor = FitForgeSecondaryDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(log.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = TextGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${log.durationMinutes} mins", fontSize = 13.sp, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${log.caloriesBurned} kcal burned", fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsTab(profile: UserProfile?) {
    val points = profile?.points ?: 0
    val badges = listOf(
        BadgeItem("Streak Master", "Active streak greater than 3 days", profile != null && profile.streak >= 3, Icons.Default.LocalFireDepartment),
        BadgeItem("Water Champ", "Logged water daily to meet goal", true, Icons.Default.LocalDrink),
        BadgeItem("Iron Lifter", "Complete 5 or more heavy workout routines", points >= 100, Icons.Default.FitnessCenter),
        BadgeItem("AI Explorer", "Query the FitForge AI assistant for plans", points >= 50, Icons.Default.SmartToy)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FORGE POINTS BALANCE", fontSize = 11.sp, color = TextGray)
                    Text("$points XP", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Icon(Icons.Default.Star, null, tint = AccentOrange, modifier = Modifier.size(48.dp))
            }
        }

        Text("BADGES COMPLETED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(badges) { badge ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) FitForgeCardDark else FitForgeCardDark.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (badge.isUnlocked) FitForgePrimaryDark.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = badge.icon,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) FitForgeSecondaryDark else TextGray
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(badge.name, fontWeight = FontWeight.Bold, color = if (badge.isUnlocked) Color.White else TextGray)
                            Text(badge.desc, fontSize = 11.sp, color = TextGray)
                        }
                        
                        if (badge.isUnlocked) {
                            Icon(Icons.Default.CheckCircle, "Unlocked", tint = AccentGreen)
                        } else {
                            Icon(Icons.Default.Lock, "Locked", tint = TextGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

data class BadgeItem(val name: String, val desc: String, val isUnlocked: Boolean, val icon: ImageVector)

// --- AI Chat Assistant Screen (Sci-Fi Glassmorphism styled prompts and message balloons) ---
@Composable
fun AICoachScreen(viewModel: FitForgeViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "FITFORGE AI ASSISTANT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "Elite AI strength & diet contextual coaching.",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            
            // Clear chat action
            IconButton(onClick = { viewModel.clearChatMessages() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = TextGray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chat Balloon List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SmartToy, null, tint = FitForgeSecondaryDark, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("I am your premium AI coach.", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Ask me for customized workout plans, macro targets, or diet guidelines.", fontSize = 12.sp, color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Chat suggestion pills
                    Text("TRY THESE EXAMPLES:", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    SuggestionPill("Suggest a muscle gain diet") { viewModel.chatPrompt = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    SuggestionPill("Custom leg day routine") { viewModel.chatPrompt = it }
                }
            } else {
                val scrollState = rememberScrollState()
                
                // Keep the scroll pushed to latest messages
                LaunchedEffect(messages.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    messages.forEach { msg ->
                        ChatBubble(message = msg)
                    }
                    if (viewModel.isChatLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(color = FitForgeSecondaryDark, modifier = Modifier.size(16.dp))
                            Text("Coach is thinking...", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Input textbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FitForgeCardDark, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = viewModel.chatPrompt,
                onValueChange = { viewModel.chatPrompt = it },
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.sendChatMessage() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                decorationBox = { innerTextField ->
                    if (viewModel.chatPrompt.isEmpty()) {
                        Text("Query coach (e.g. customized gym plans...)", color = TextGray, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            
            IconButton(
                onClick = { viewModel.sendChatMessage() },
                modifier = Modifier.testTag("chat_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = FitForgeSecondaryDark)
            }
        }
    }
}

@Composable
fun SuggestionPill(label: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick(label) },
        colors = CardDefaults.cardColors(containerColor = FitForgeCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = FitForgeSecondaryDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessageLog) {
    val isUser = message.sender == "user"
    val bubbleColor = if (isUser) FitForgePrimaryDark else FitForgeCardDark
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val cornerShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, cornerShape)
                .padding(14.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(viewModel: FitForgeViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    var languageExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitForgeBackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "SETTINGS & PROFILE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Profile block summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(FitForgePrimaryDark.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = FitForgeSecondaryDark, modifier = Modifier.size(36.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(profile?.name ?: "Fit Forger", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text(profile?.email ?: "user@fitforge.ai", fontSize = 12.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        BadgeText(
                            text = if (viewModel.isSubscribed) "ELITE MEMBER" else "STANDARD ACCOUNT",
                            containerColor = if (viewModel.isSubscribed) FitForgeSecondaryDark.copy(alpha = 0.2f) else FitForgeCardDark,
                            contentColor = if (viewModel.isSubscribed) FitForgeSecondaryDark else TextGray
                        )
                    }
                }
            }
        }

        // Configuration cards
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("SYSTEM PREFERENCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                    
                    // Metric vs Imperial selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Measurement Units", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Switch Metric (kg, cm) vs Imperial (lbs, in)", fontSize = 11.sp, color = TextGray)
                        }
                        Switch(
                            checked = viewModel.isMetric,
                            onCheckedChange = { viewModel.isMetric = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FitForgeSecondaryDark),
                            modifier = Modifier.testTag("toggle_units_switch")
                        )
                    }

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Language Choice", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(viewModel.selectedLanguage, fontSize = 12.sp, color = FitForgeSecondaryDark)
                        }
                        Box {
                            Button(
                                onClick = { languageExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FitForgeCardDark)
                            ) {
                                Text("Change", color = Color.White)
                            }
                            DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                                DropdownMenuItem(text = { Text("English") }, onClick = { viewModel.selectedLanguage = "English"; languageExpanded = false })
                                DropdownMenuItem(text = { Text("Español") }, onClick = { viewModel.selectedLanguage = "Español"; languageExpanded = false })
                                DropdownMenuItem(text = { Text("Français") }, onClick = { viewModel.selectedLanguage = "Français"; languageExpanded = false })
                                DropdownMenuItem(text = { Text("Deutsch") }, onClick = { viewModel.selectedLanguage = "Deutsch"; languageExpanded = false })
                            }
                        }
                    }
                }
            }
        }

        // Subscription controller card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FitForgeCardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MONETIZATION & PAYMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FitForgeSecondaryDark, letterSpacing = 1.sp)
                    Text("Manage subscription states to unlock elite parameters.", fontSize = 12.sp, color = TextGray)
                    
                    if (viewModel.isSubscribed) {
                        Button(
                            onClick = { viewModel.downgradeSubscription() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CANCEL ELITE SUBSCRIPTION", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.upgradeSubscription() },
                            colors = ButtonDefaults.buttonColors(containerColor = FitForgeSecondaryDark, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ACTIVATE ELITE (REMOVE ADS)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Logout
        item {
            Button(
                onClick = { viewModel.handleLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT ACCOUNT")
            }
        }
    }
}

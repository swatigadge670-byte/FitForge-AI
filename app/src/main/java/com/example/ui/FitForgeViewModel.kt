package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val appDao = database.appDao()
    val repository = FitForgeRepository(appDao)

    // --- Authentication State ---
    var isLoggedIn by mutableStateOf(false)
    var authMode by mutableStateOf("welcome") // welcome, login, register, forgot
    var authEmail by mutableStateOf("")
    var authPassword by mutableStateOf("")
    var authName by mutableStateOf("")
    var authError by mutableStateOf("")
    var isAuthLoading by mutableStateOf(false)

    // --- Unit Selection & Language Settings ---
    var isMetric by mutableStateOf(true) // true = Metric (kg, cm), false = Imperial (lbs, inches)
    var selectedLanguage by mutableStateOf("English")
    var enableWaterReminder by mutableStateOf(true)
    var enableWorkoutReminder by mutableStateOf(true)
    var enableMealReminder by mutableStateOf(true)

    // --- Premium & Ads State ---
    var isSubscribed by mutableStateOf(false)
    var showAdBanner by mutableStateOf(true)

    // --- Active Workout State ---
    var activeWorkoutPlan by mutableStateOf<WorkoutPlan?>(null)
    var currentExerciseIndex by mutableStateOf(0)
    var completedSets = mutableStateOf(mutableMapOf<Int, Int>()) // exerciseIndex to completed sets count
    var activeRestTimeLeft by mutableIntStateOf(0)
    var isRestTimerRunning by mutableStateOf(false)
    private var timerJob: kotlinx.coroutines.Job? = null

    // --- Chat State ---
    var chatPrompt by mutableStateOf("")
    var isChatLoading by mutableStateOf(false)

    // --- Calculators State ---
    var calcHeight by mutableStateOf("175")
    var calcWeight by mutableStateOf("70")
    var calcAge by mutableStateOf("25")
    var calcGender by mutableStateOf("Male")
    var calcGoal by mutableStateOf("Muscle Gain")
    var calcActivityLevel by mutableStateOf("Moderately Active")

    var calcBmi by mutableStateOf(0f)
    var calcBmiCategory by mutableStateOf("")
    var calcCalories by mutableStateOf(0)
    var calcProtein by mutableStateOf(0) // grams
    var calcCarbs by mutableStateOf(0) // grams
    var calcFats by mutableStateOf(0) // grams

    // --- Database States (Reactive Flows) ---
    val todayActivity: StateFlow<DailyActivityLog?> = repository.getDailyActivityFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutHistory: StateFlow<List<WorkoutHistory>> = repository.getWorkoutHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightHistory: StateFlow<List<WeightLog>> = repository.getWeightHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageLog>> = repository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Initialize default today activity and profile
        viewModelScope.launch {
            repository.getOrCreateDailyActivity()
            val existingProfile = appDao.getUserProfileDirect()
            if (existingProfile == null) {
                // Pre-populate standard user profile
                repository.saveUserProfile(UserProfile())
            } else {
                isSubscribed = existingProfile.isPremium
                showAdBanner = !existingProfile.isPremium
            }
            
            // Generate standard mock weight history if empty
            val existingWeight = appDao.getWeightHistory().firstOrNull() ?: emptyList()
            if (existingWeight.isEmpty()) {
                val baseWeight = existingProfile?.weightKg ?: 70f
                val cal = Calendar.getInstance()
                for (i in 5 downTo 0) {
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    appDao.insertWeightLog(WeightLog(dateStr, baseWeight - (i * 0.2f) + Random().nextFloat() * 0.4f))
                    cal.add(Calendar.DAY_OF_YEAR, i) // reset
                }
            }
            
            // Run standard initial calculators
            calculateAll()
        }
    }

    // --- Authentication Actions ---
    fun handleLogin() {
        if (authEmail.isEmpty() || authPassword.isEmpty()) {
            authError = "Please fill in all fields"
            return
        }
        isAuthLoading = true
        viewModelScope.launch {
            // Simulate authentic Firebase loading state
            kotlinx.coroutines.delay(1000)
            isLoggedIn = true
            isAuthLoading = false
            authError = ""
            
            // Load user profile
            val existingProfile = appDao.getUserProfileDirect() ?: UserProfile(email = authEmail)
            repository.saveUserProfile(existingProfile)
            isSubscribed = existingProfile.isPremium
            showAdBanner = !existingProfile.isPremium
        }
    }

    fun handleRegister() {
        if (authName.isEmpty() || authEmail.isEmpty() || authPassword.isEmpty()) {
            authError = "Please fill in all fields"
            return
        }
        isAuthLoading = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            isLoggedIn = true
            isAuthLoading = false
            authError = ""
            
            val newProfile = UserProfile(
                name = authName,
                email = authEmail
            )
            repository.saveUserProfile(newProfile)
            isSubscribed = false
            showAdBanner = true
        }
    }

    fun handleGoogleSignIn() {
        isAuthLoading = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            isLoggedIn = true
            isAuthLoading = false
            authError = ""
            
            val newProfile = UserProfile(
                name = "Google User",
                email = "google.user@gmail.com"
            )
            repository.saveUserProfile(newProfile)
            isSubscribed = false
            showAdBanner = true
        }
    }

    fun handleForgotPassword() {
        if (authEmail.isEmpty()) {
            authError = "Please enter your email"
            return
        }
        isAuthLoading = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            isAuthLoading = false
            authError = "Password reset link sent to $authEmail"
        }
    }

    fun handleLogout() {
        isLoggedIn = false
        authMode = "welcome"
        authEmail = ""
        authPassword = ""
        authName = ""
    }

    // --- Tracking Actions ---
    fun addWaterLog(amount: Int) {
        viewModelScope.launch {
            repository.addWater(amount)
        }
    }

    fun addStepLog(count: Int) {
        viewModelScope.launch {
            repository.addSteps(count)
        }
    }

    fun logUserWeight(weight: Float) {
        viewModelScope.launch {
            repository.updateWeight(weight)
        }
    }

    // --- Active Workout Actions ---
    fun startWorkout(plan: WorkoutPlan) {
        activeWorkoutPlan = plan
        currentExerciseIndex = 0
        completedSets.value = mutableMapOf()
        activeRestTimeLeft = 0
        isRestTimerRunning = false
        timerJob?.cancel()
    }

    fun completeExerciseSet() {
        val plan = activeWorkoutPlan ?: return
        val currentMap = completedSets.value.toMutableMap()
        val currentCount = currentMap[currentExerciseIndex] ?: 0
        val targetSets = plan.exercises[currentExerciseIndex].defaultSets
        
        if (currentCount < targetSets) {
            currentMap[currentExerciseIndex] = currentCount + 1
            completedSets.value = currentMap
            
            // Trigger standard premium rest timer
            startRestTimer(plan.exercises[currentExerciseIndex].defaultRestSecs)
        }
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        activeRestTimeLeft = seconds
        isRestTimerRunning = true
        timerJob = viewModelScope.launch {
            while (activeRestTimeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                activeRestTimeLeft--
            }
            isRestTimerRunning = false
        }
    }

    fun skipRestTimer() {
        timerJob?.cancel()
        activeRestTimeLeft = 0
        isRestTimerRunning = false
    }

    fun nextExercise() {
        val plan = activeWorkoutPlan ?: return
        if (currentExerciseIndex < plan.exercises.size - 1) {
            currentExerciseIndex++
            timerJob?.cancel()
            isRestTimerRunning = false
            activeRestTimeLeft = 0
        }
    }

    fun previousExercise() {
        if (currentExerciseIndex > 0) {
            currentExerciseIndex--
            timerJob?.cancel()
            isRestTimerRunning = false
            activeRestTimeLeft = 0
        }
    }

    fun finishWorkout() {
        val plan = activeWorkoutPlan ?: return
        viewModelScope.launch {
            repository.completeWorkout(plan)
            activeWorkoutPlan = null
            timerJob?.cancel()
        }
    }

    fun quitWorkout() {
        activeWorkoutPlan = null
        timerJob?.cancel()
    }

    // --- AI Assistant Actions ---
    fun sendChatMessage() {
        val prompt = chatPrompt.trim()
        if (prompt.isEmpty() || isChatLoading) return
        
        chatPrompt = ""
        isChatLoading = true
        viewModelScope.launch {
            repository.sendMessageToAICoach(prompt)
            isChatLoading = false
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- Calculators Logic ---
    fun calculateAll() {
        val h = calcHeight.toFloatOrNull() ?: 175f
        val w = calcWeight.toFloatOrNull() ?: 70f
        val a = calcAge.toIntOrNull() ?: 25
        
        // 1. BMI Calculation
        val heightM = h / 100f
        calcBmi = w / (heightM * heightM)
        calcBmiCategory = when {
            calcBmi < 18.5f -> "Underweight"
            calcBmi < 24.9f -> "Normal Weight"
            calcBmi < 29.9f -> "Overweight"
            else -> "Obese"
        }

        // 2. Daily Calorie Needs (Harris-Benedict Equation)
        val bmr = if (calcGender == "Male") {
            88.362f + (13.397f * w) + (4.799f * h) - (5.677f * a)
        } else {
            447.593f + (9.247f * w) + (3.098f * h) - (4.330f * a)
        }

        val activityMultiplier = when (calcActivityLevel) {
            "Sedentary" -> 1.2f
            "Lightly Active" -> 1.375f
            "Moderately Active" -> 1.55f
            "Very Active" -> 1.725f
            else -> 1.9f
        }

        val tdee = (bmr * activityMultiplier).toInt()
        
        // Adjust calories based on goal
        calcCalories = when (calcGoal) {
            "Fat Loss" -> (tdee - 500).coerceAtLeast(1200)
            "Muscle Gain" -> tdee + 300
            else -> tdee // Maintenance / Strength Training
        }

        // 3. Macronutrients allocation
        // Protein: 2.0g per kg of bodyweight for muscle/strength, 1.6g for maintenance, 2.2g for fat loss to retain muscle
        val proteinPerKg = when (calcGoal) {
            "Muscle Gain" -> 2.2f
            "Fat Loss" -> 2.0f
            else -> 1.8f
        }
        calcProtein = (w * proteinPerKg).toInt()
        
        // Fat: 25% of daily calories
        val fatCalories = calcCalories * 0.25f
        calcFats = (fatCalories / 9f).toInt()

        // Carbs: Remaining calories
        val proteinCalories = calcProtein * 4f
        val carbCalories = (calcCalories - proteinCalories - fatCalories).coerceAtLeast(0f)
        calcCarbs = (carbCalories / 4f).toInt()
    }

    // --- Subscription Purchase Option ---
    fun upgradeSubscription() {
        viewModelScope.launch {
            val existing = appDao.getUserProfileDirect() ?: UserProfile()
            val updated = existing.copy(isPremium = true)
            repository.saveUserProfile(updated)
            isSubscribed = true
            showAdBanner = false
        }
    }

    fun downgradeSubscription() {
        viewModelScope.launch {
            val existing = appDao.getUserProfileDirect() ?: UserProfile()
            val updated = existing.copy(isPremium = false)
            repository.saveUserProfile(updated)
            isSubscribed = false
            showAdBanner = true
        }
    }
}

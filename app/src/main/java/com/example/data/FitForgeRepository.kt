package com.example.data

import android.content.Context
import android.util.Log
import com.example.service.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitForgeRepository(private val appDao: AppDao) {

    // Helper to get current date as YYYY-MM-DD
    fun getCurrentDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // --- Daily Activity ---
    fun getDailyActivityFlow(date: String = getCurrentDateStr()): Flow<DailyActivityLog?> {
        return appDao.getDailyActivity(date)
    }

    suspend fun getOrCreateDailyActivity(date: String = getCurrentDateStr()): DailyActivityLog {
        val existing = appDao.getDailyActivityDirect(date)
        if (existing != null) return existing
        
        // Retrieve current profile for base weight
        val profile = appDao.getUserProfileDirect() ?: UserProfile()
        val newLog = DailyActivityLog(
            date = date,
            steps = 0,
            caloriesBurned = 0,
            waterIntakeMl = 0,
            waterTargetMl = 2000,
            stepTarget = 10000,
            weightKg = profile.weightKg
        )
        appDao.insertDailyActivity(newLog)
        return newLog
    }

    suspend fun addWater(amountMl: Int, date: String = getCurrentDateStr()) {
        val log = getOrCreateDailyActivity(date)
        val updated = log.copy(waterIntakeMl = log.waterIntakeMl + amountMl)
        appDao.insertDailyActivity(updated)
    }

    suspend fun addSteps(count: Int, date: String = getCurrentDateStr()) {
        val log = getOrCreateDailyActivity(date)
        // Also estimate calories burned: approx 0.04 calories per step
        val extraCal = (count * 0.04).toInt()
        val updated = log.copy(
            steps = log.steps + count,
            caloriesBurned = log.caloriesBurned + extraCal
        )
        appDao.insertDailyActivity(updated)
    }

    suspend fun addCaloriesBurned(calories: Int, date: String = getCurrentDateStr()) {
        val log = getOrCreateDailyActivity(date)
        val updated = log.copy(caloriesBurned = log.caloriesBurned + calories)
        appDao.insertDailyActivity(updated)
    }

    suspend fun updateWeight(weightKg: Float, date: String = getCurrentDateStr()) {
        val log = getOrCreateDailyActivity(date)
        val updated = log.copy(weightKg = weightKg)
        appDao.insertDailyActivity(updated)
        
        // Save to historic weight log
        appDao.insertWeightLog(WeightLog(date, weightKg))
        
        // Also update profile
        val profile = appDao.getUserProfileDirect() ?: UserProfile()
        appDao.insertUserProfile(profile.copy(weightKg = weightKg))
    }

    // --- Workout History ---
    fun getWorkoutHistoryFlow(): Flow<List<WorkoutHistory>> {
        return appDao.getWorkoutHistory()
    }

    suspend fun completeWorkout(plan: WorkoutPlan, durationMinutes: Int = plan.durationMinutes) {
        val dateStr = getCurrentDateStr()
        val calories = plan.estCaloriesBurned
        
        val history = WorkoutHistory(
            date = dateStr,
            title = plan.title,
            type = plan.split,
            durationMinutes = durationMinutes,
            caloriesBurned = calories
        )
        appDao.insertWorkoutHistory(history)
        
        // Add to daily activity
        addCaloriesBurned(calories, dateStr)
        
        // Increment streak and award points
        val profile = appDao.getUserProfileDirect() ?: UserProfile()
        val updatedProfile = profile.copy(
            streak = profile.streak + 1,
            points = profile.points + 50
        )
        appDao.insertUserProfile(updatedProfile)
    }

    // --- Weight Log ---
    fun getWeightHistoryFlow(): Flow<List<WeightLog>> {
        return appDao.getWeightHistory()
    }

    // --- Chat Logs & AI Coach ---
    fun getChatMessagesFlow(): Flow<List<ChatMessageLog>> {
        return appDao.getChatMessages()
    }

    suspend fun clearChat() {
        appDao.clearChatHistory()
    }

    suspend fun sendMessageToAICoach(userPrompt: String): String {
        // Save user message
        val userMsg = ChatMessageLog(sender = "user", message = userPrompt)
        appDao.insertChatMessage(userMsg)
        
        // Retrieve profile for contextual guidance
        val profile = appDao.getUserProfileDirect() ?: UserProfile()
        val systemInstruction = """
            You are FitForge AI Coach, a premium, professional elite fitness trainer, nutritionist, and health assistant.
            You are helping ${profile.name}, who is a ${profile.age} year old ${profile.gender}, height ${profile.heightCm}cm, weight ${profile.weightKg}kg.
            Their current fitness goal is: ${profile.fitnessGoal}. Activity level: ${profile.activityLevel}.
            Provide concise, elite-level fitness guidance, diet tips, and motivation. Keep responses elegant, structured, professional, and practical.
            Always maintain a positive, motivating tone. Do not use complex jargon.
        """.trimIndent()
        
        // Call Gemini Service
        val aiResponse = GeminiService.generateResponse(userPrompt, systemInstruction)
        
        // Save AI message
        val aiMsg = ChatMessageLog(sender = "ai", message = aiResponse)
        appDao.insertChatMessage(aiMsg)
        
        return aiResponse
    }

    // --- User Profile ---
    fun getUserProfileFlow(): Flow<UserProfile?> {
        return appDao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        appDao.insertUserProfile(profile)
        // Ensure weight is synced with today's log
        val dateStr = getCurrentDateStr()
        val log = getOrCreateDailyActivity(dateStr)
        appDao.insertDailyActivity(log.copy(weightKg = profile.weightKg))
        appDao.insertWeightLog(WeightLog(dateStr, profile.weightKg))
    }
}

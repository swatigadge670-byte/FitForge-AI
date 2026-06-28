package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity")
data class DailyActivityLog(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val waterIntakeMl: Int = 0,
    val waterTargetMl: Int = 2000,
    val stepTarget: Int = 10000,
    val weightKg: Float = 70.0f
)

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val title: String,
    val type: String, // Push, Bro Split, Full Body, Home, etc.
    val durationMinutes: Int,
    val caloriesBurned: Int
)

@Entity(tableName = "weight_history")
data class WeightLog(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val weightKg: Float
)

@Entity(tableName = "chat_message")
data class ChatMessageLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Fit Forger",
    val email: String = "user@fitforge.ai",
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val age: Int = 25,
    val gender: String = "Male",
    val activityLevel: String = "Moderately Active",
    val fitnessGoal: String = "Build Muscle",
    val streak: Int = 3,
    val points: Int = 120,
    val isPremium: Boolean = false
)

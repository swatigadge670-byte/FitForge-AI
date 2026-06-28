package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Daily Activity Logs
    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun getDailyActivity(date: String): Flow<DailyActivityLog?>

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getDailyActivityDirect(date: String): DailyActivityLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyActivity(log: DailyActivityLog)

    // Workout History
    @Query("SELECT * FROM workout_history ORDER BY id DESC")
    fun getWorkoutHistory(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutHistory(workout: WorkoutHistory)

    // Weight Logs
    @Query("SELECT * FROM weight_history ORDER BY date ASC")
    fun getWeightHistory(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weight: WeightLog)

    // Chat Messages
    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageLog)

    @Query("DELETE FROM chat_message")
    suspend fun clearChatHistory()

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun getUserProfile(id: String = "current_user"): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getUserProfileDirect(id: String = "current_user"): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

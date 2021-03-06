package com.kiwicorp.dumbstrength.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kiwicorp.dumbstrength.data.Workout
import com.kiwicorp.dumbstrength.data.WorkoutWithEntries

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY workout_date DESC")
    fun observerWorkouts(): LiveData<List<WorkoutWithEntries>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY workout_date DESC")
    suspend fun getWorkouts(): List<WorkoutWithEntries>

    @Transaction
    @Query("SELECT * FROM workouts WHERE workout_id = :workoutId")
    suspend fun getWorkout(workoutId: String): WorkoutWithEntries

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout): Int

    @Delete
    suspend fun deleteWorkout(workout: Workout): Int
}
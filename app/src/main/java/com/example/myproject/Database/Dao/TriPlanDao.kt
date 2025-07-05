package com.example.myproject.Database.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myproject.Database.Entities.TripPlan

@Dao
interface TripPlanDao {
    @Insert
    fun insert(plan: TripPlan): Long

    @Query("SELECT * FROM trip_plan_table ORDER BY startDate ASC")
    fun getAllPlans(): LiveData<List<TripPlan>>

    @Delete
    fun delete(plan: TripPlan)

    @Query("SELECT * FROM trip_plan_table ORDER BY startDate ASC")
    suspend fun getAllPlansRaw(): List<TripPlan>

    @Query("SELECT * FROM trip_plan_table WHERE startDate = :date")
    fun getAllPlansForDate(date: String): List<TripPlan>

    @Query("SELECT * FROM trip_plan_table WHERE id = :id LIMIT 1")
    fun getById(id: Int): TripPlan?
}


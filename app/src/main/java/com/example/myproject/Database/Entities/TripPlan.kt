package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_plan_table")
data class TripPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: String,
    val endDate: String,
    val startPlace: String?,
    val endPlace: String,
    val description: String?,
    val type: TripType
)
package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_table")
class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val start: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val description: String?,
    val type: TripType
)

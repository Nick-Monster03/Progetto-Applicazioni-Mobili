package com.example.myproject.Trip

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DailyTrip")
data class DailyTrip(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                     val title: String,
                     val destination: String,
                     val distance: Double,
                     val duration: Long,
                     val motivation: String,
                     val cityDestination: String)



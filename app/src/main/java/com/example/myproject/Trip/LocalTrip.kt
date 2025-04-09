package com.example.myproject.Trip

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "LocalTrip")
data class LocalTrip(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                     val title: String,
                     val destination: String,
                     val distance: Double,
                     val duration: Long,
                     val startDate: Date,
                     val pointInterest: String)

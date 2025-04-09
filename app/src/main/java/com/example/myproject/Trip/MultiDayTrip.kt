package com.example.myproject.Trip

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "MultiDayTrip")
data class MultiDayTrip(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                        val title: String,
                        val destination: String,
                        val distance: Double,
                        val duration: Long,
                        val motivation: String,
                        val countryDestination: String,
                        val intermediateDestinations: List<String>,
                        val endingDate: Date)
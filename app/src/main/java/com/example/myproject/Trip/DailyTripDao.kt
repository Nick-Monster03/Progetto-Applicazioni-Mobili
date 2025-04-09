package com.example.myproject.Trip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DailyTripDao {
    @Insert
    fun insertTrip(trip: DailyTrip): Long

    @Query("SELECT * FROM DailyTrip")
    fun getTrips(): List<DailyTrip>
}
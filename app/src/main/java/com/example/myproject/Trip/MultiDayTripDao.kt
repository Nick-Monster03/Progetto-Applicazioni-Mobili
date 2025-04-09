package com.example.myproject.Trip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MultiDayTripDao {
    @Insert
    fun insertTrip(trip: MultiDayTrip): Long

    @Query("SELECT * FROM MultiDayTrip")
    fun getTrips(): List<MultiDayTrip>
}
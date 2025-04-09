package com.example.myproject.Trip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocalTripDao {
    @Insert
    fun insertTrip(trip: LocalTrip): Long

    @Query("SELECT * FROM LocalTrip")
    fun getTrips(): List<LocalTrip>
}
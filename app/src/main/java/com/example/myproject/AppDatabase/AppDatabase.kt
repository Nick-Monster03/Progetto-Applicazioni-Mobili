package com.example.myproject.AppDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myproject.Trip.Converters
import com.example.myproject.Trip.DailyTrip
import com.example.myproject.Trip.DailyTripDao
import com.example.myproject.Trip.LocalTrip
import com.example.myproject.Trip.LocalTripDao
import com.example.myproject.Trip.MultiDayTrip
import com.example.myproject.Trip.MultiDayTripDao

@Database(entities = [LocalTrip::class, DailyTrip::class, MultiDayTrip::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localtripDao(): LocalTripDao
    abstract fun dailyTripDao(): DailyTripDao
    abstract fun multidaytripDao(): MultiDayTripDao
}
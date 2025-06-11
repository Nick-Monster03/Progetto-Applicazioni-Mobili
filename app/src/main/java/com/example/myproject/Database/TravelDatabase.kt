package com.example.myproject.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myproject.Database.Dao.PhotoDao
import com.example.myproject.Database.Dao.PlaceDao
import com.example.myproject.Database.Dao.TripDao
import com.example.myproject.Database.Dao.TripPlaceDao
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(
    entities = [Trip::class, Place::class, Photo::class, TripPlace::class], version = 2, exportSchema = false)

abstract class TravelDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun placeDao(): PlaceDao
    abstract fun photoDao(): PhotoDao
    abstract fun tripPlaceDao(): TripPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: TravelDatabase? = null
        private const val N_THREADS = 4
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(N_THREADS)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE trip_table ADD COLUMN description TEXT")
            }
        }

        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Esempio: prepopola il database al primo avvio
                databaseWriteExecutor.execute {

                }
            }
        }

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

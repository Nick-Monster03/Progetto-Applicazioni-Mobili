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
    entities = [Trip::class, Place::class, Photo::class, TripPlace::class], version = 3, exportSchema = false)

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crea nuova tabella con timestamp TEXT
                database.execSQL("""
            CREATE TABLE photo_table_new (
                id_place INTEGER NOT NULL,
                photoBlob BLOB NOT NULL,
                timestamp TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(id_place, photoBlob),
                FOREIGN KEY(id_place) REFERENCES place_table(id) ON DELETE CASCADE
            )
        """.trimIndent())

                // 2. Copia i dati dalla vecchia tabella
                database.execSQL("""
            INSERT INTO photo_table_new (id_place, photoBlob, timestamp)
            SELECT id_place, photoBlob, CAST(timestamp AS TEXT) FROM photo_table
        """.trimIndent())

                // 3. Elimina la tabella vecchia
                database.execSQL("DROP TABLE photo_table")

                // 4. Rinomina la nuova tabella con il nome originale
                database.execSQL("ALTER TABLE photo_table_new RENAME TO photo_table")
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
                ).addMigrations(MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

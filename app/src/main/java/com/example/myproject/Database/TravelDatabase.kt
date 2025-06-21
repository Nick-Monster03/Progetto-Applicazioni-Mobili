package com.example.myproject.Database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myproject.Database.Dao.*
import com.example.myproject.Database.Entities.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(
    entities = [Trip::class, Place::class, Photo::class, TripPlace::class],
    version = 3,
    exportSchema = false
)
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

        // MIGRAZIONE DA 1 a 2 (esempio: nessuna modifica)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_place_table_latitudine_longitudine ON place_table(latitudine, longitudine)")
            }
        }

        // MIGRAZIONE DA 2 a 3 (già presente)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE photo_table_new (
                        id_place INTEGER NOT NULL,
                        photoBlob BLOB NOT NULL,
                        timestamp TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(id_place, photoBlob),
                        FOREIGN KEY(id_place) REFERENCES place_table(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO photo_table_new (id_place, photoBlob, timestamp)
                    SELECT id_place, photoBlob, CAST(timestamp AS TEXT) FROM photo_table
                """.trimIndent())

                database.execSQL("DROP TABLE photo_table")
                database.execSQL("ALTER TABLE photo_table_new RENAME TO photo_table")
            }
        }

        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                databaseWriteExecutor.execute {
                    // Pre-popolamento se necessario
                }
            }
        }

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

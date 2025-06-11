package com.example.myproject.Database.Dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table")
    fun getListOfTrips(): LiveData<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(trip: Trip)

    @Query("SELECT MAX(id) FROM trip_table")
    fun getLastTripId(): Int

    @Query("SELECT * FROM trip_table WHERE type = :type")
    fun getTripsByType(type: TripType): LiveData<List<Trip>>

    @Query("UPDATE trip_table SET destination = :destination, endDate = :endDate WHERE id = :id")
    fun updateTrip(id: Int, destination: String, endDate: String)

    @Query("DELETE FROM trip_table WHERE id = :id")
    fun delete(id: Int)

}
package com.example.myproject.Database.Dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myproject.Database.Entities.TripPlace

@Dao
interface TripPlaceDao {
    @Query("SELECT * FROM TripPlace")
    fun getAllTripPlaces(): LiveData<List<TripPlace>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tripPlace: TripPlace)


    @Query("DELETE FROM TripPlace WHERE tripId = :tripId AND placeId = :placeId")
    fun delete(tripId: Int, placeId: Int)

    @Query("DELETE FROM TripPlace WHERE tripId = :tripId")
    fun delete(tripId: Int)
}
package com.example.myproject.Database.Dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myproject.Database.Entities.Photo

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    @Query("SELECT * FROM photo_table WHERE id_place = :placeId ")
    fun getPhotosByPlace(placeId: Int): LiveData<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE id_trip = :tripId")
    fun getPhotosByTrip(tripId: Int): LiveData<List<Photo>>

    @Query("SELECT * FROM photo_table")
    fun getPhotosByPlace(): LiveData<List<Photo>>

    @Query("SELECT * FROM photo_table as p1 JOIN place_table as p2 on p1.id_place = p2.id WHERE id_place = :placeId ")
    fun getPhotosAndPlaces(placeId: Int): LiveData<List<Photo>>

    @Query("DELETE FROM photo_table")
    fun deleteDone()

}
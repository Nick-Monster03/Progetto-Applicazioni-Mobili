package com.example.myproject.Database.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myproject.Database.Entities.Place

@Dao
interface PlaceDao {

    @Query("SELECT * FROM place_table")
    fun getListOfPlaces(): LiveData<List<Place>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(place: Place)

    @Query("SELECT id FROM place_table WHERE latitudine = :latitudine AND longitudine = :longitudine")
    fun getPlacesByCoordinates(latitudine: Double, longitudine: Double): Int

    @Query("DELETE FROM place_table WHERE id = :id")
    fun deletePlaceById(id: Int)

    @Query ("SELECT count(*) FROM place_table")
    fun getPlaceCount(): Int
}
package com.example.myproject.repository

import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Dao.PlaceDao

class PlaceRepository(private val placeDao: PlaceDao) {

    val allPlaces: LiveData<List<Place>> = placeDao.getListOfPlaces()

    fun insert(place: Place) {
        placeDao.insert(place)
    }

    fun delete(id: Int) {
        placeDao.deletePlaceById(id)
    }

    fun getPlace(place : Place) {
        placeDao.getPlacesByCoordinates(latitudine = place.latitudine, longitudine = place.longitudine)
    }
}

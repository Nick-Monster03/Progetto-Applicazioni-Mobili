package com.example.myproject.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Dao.PlaceDao
import com.example.myproject.Database.TravelDatabase

class PlaceRepository(app: Application) {

    var placeDao: PlaceDao

    init {
        val db = TravelDatabase.getDatabase(app)
        placeDao = db.placeDao()
    }


    fun insert(place: Place) {
        placeDao.insert(place)
    }

    fun delete(id: Int) {
        placeDao.deletePlaceById(id)
    }

    fun getPlaceId(place : Place): Int {
        return placeDao.getPlacesByCoordinates(latitudine = place.latitudine, longitudine = place.longitudine)
    }

    fun getAllPlaces(): LiveData<List<Place>> {
        return placeDao.getListOfPlaces()
    }

    fun existsPlace(place: Place): Boolean {
        return placeDao.getCountPlacesByCoordinates(latitudine = place.latitudine, longitudine = place.longitudine) > 0
    }

    fun getPlaceByCordinates(latitudine: Double, longitudine: Double): Int {
        return placeDao.getPlacesByCoordinates(latitudine, longitudine)
    }

    fun getPlacedByIdTrip(tripId: Int): LiveData<List<Place>> {
        return placeDao.getPlacedByIdTrip(tripId)
    }
    fun existsPlaceById(id: Int): Boolean {
        return placeDao.getPlaceCount(id) > 0
    }

    fun getPlaceSinceDate(date: String): LiveData<List<Place>> {
        return placeDao.getTripsSince(date)
    }
}

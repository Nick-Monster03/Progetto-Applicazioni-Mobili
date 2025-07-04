package com.example.myproject.Repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.TripPlaceDao
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.TravelDatabase

class TripPlaceRepository(app:Application) {

    var tripPlaceDao: TripPlaceDao

    init {
        val db = TravelDatabase.getDatabase(app)
        tripPlaceDao = db.tripPlaceDao()
    }

    fun getAllTripPlaces(): LiveData<List<TripPlace>> {
        return tripPlaceDao.getAllTripPlaces()
    }

    fun insertTripPlace(tripPlace: TripPlace) {
        tripPlaceDao.insert(tripPlace)
    }

    fun deleteTripPlace(tripId: Int, placeId: Int) {
        tripPlaceDao.delete(tripId, placeId)
    }

    fun deleteTripPlacesByTripId(tripId: Int) {
        tripPlaceDao.delete(tripId)
    }

    fun getTripPlacesForTrip(tripId: Int): LiveData<List<TripPlace>>
    {
        return tripPlaceDao.getTripPlacesForTrip(tripId)
    }
}
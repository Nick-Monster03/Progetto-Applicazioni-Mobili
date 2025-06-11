package com.example.myproject

import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.TripPlaceDao
import com.example.myproject.Database.Entities.TripPlace

class TripPlaceRepository(private val tripPlaceDao: TripPlaceDao) {

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
}
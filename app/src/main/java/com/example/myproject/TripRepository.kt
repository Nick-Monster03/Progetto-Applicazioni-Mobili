package com.example.myproject

import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.TripDao
import com.example.myproject.Database.Entities.Trip

class TripRepository(private val tripDao: TripDao) {

    fun insertTrip(trip: Trip) {
        tripDao.insert(trip)
    }

    fun getAllTrips(): LiveData<List<Trip>> {
        return tripDao.getListOfTrips()
    }

    fun getLastTrip(): Int {
        return tripDao.getLastTripId()
    }

    fun deleteTrip(id: Int) {
        tripDao.delete(id)
    }

    fun updateTrip(id: Int, destination: String, endDate: String) {
        tripDao.updateTrip(id, destination, endDate)
    }

}

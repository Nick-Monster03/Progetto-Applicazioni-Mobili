package com.example.myproject.ui.TripsStorical

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.TripDao
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Database.TravelDatabase

class TripRepository(app: Application) {

    var tripDao: TripDao

    init {
        val db = TravelDatabase.getDatabase(app)
        tripDao = db.tripDao()
    }

    fun insertTrip(trip: Trip): Long {
        return tripDao.insert(trip)
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

    fun addDescription(id: Int, description: String){
        tripDao.addDescription(id, description)
    }

    fun getCurrentTripId(): Int{
        return tripDao.getCurrentTripId() ?: -1
    }

    fun countCurrentTrip(): Int {
        return tripDao.getNumberCurrentTrip()
    }

    fun getFilteredTrips(type: TripType?, fromDate: String?, toDate: String?
    ): LiveData<List<Trip>> {
        return tripDao.getFilteredTrips(type, fromDate, toDate)
    }

    fun getPlacedById(tripId: Int): LiveData<List<Place>> {
        return tripDao.getPlacesForTrip(tripId)
    }

    fun getMostRecentEndedTrip(): Trip? {
        return tripDao.getMostRecentEndedTrip()
    }

    fun existsTripWithId(tripId: Int): Boolean {
        return tripDao.getTripCountById(tripId) > 0
    }

    fun getTripsSince(fromDate: String): LiveData<List<Trip>> {
        return tripDao.getTripsSince(fromDate)
    }

    fun getAllEndedTrips(): LiveData<List<Trip>> {
        return tripDao.getAllEndedTrips()
    }

    fun getTripCount(tripId: Int): Int {
        return tripDao.getTripCountById(tripId)
    }
}

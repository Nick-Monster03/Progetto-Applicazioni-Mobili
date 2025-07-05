package com.example.myproject.Repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.TripPlanDao
import com.example.myproject.Database.Entities.TripPlan
import com.example.myproject.Database.TravelDatabase

class TripPlanRepository(app: Application) {

    private val tripPlanDao: TripPlanDao

    init {
        val db = TravelDatabase.getDatabase(app)
        tripPlanDao = db.tripPlanDao()
    }

    fun insertTripPlan(plan: TripPlan): Long {
        return tripPlanDao.insert(plan)
    }

    fun getAllTripPlans(): LiveData<List<TripPlan>> {
        return tripPlanDao.getAllPlans()
    }

    suspend fun getAllTripPlansRaw(): List<TripPlan> {
        return tripPlanDao.getAllPlansRaw()
    }

    fun deleteTripPlan(plan: TripPlan) {
        tripPlanDao.delete(plan)
    }

    fun getAllPlansForDate(date: String): List<TripPlan> {
        return tripPlanDao.getAllPlansForDate(date)
    }

    fun getTripPlanById(id: Int): TripPlan? {
        return tripPlanDao.getById(id)
    }
}
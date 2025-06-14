package com.example.myproject.ui.ProgramTrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.TripPlaceRepository
import com.example.myproject.repository.PlaceRepository
import com.example.myproject.ui.TripsStorical.TripRepository

class ProgramTripViewModel(private val trip_repository: TripRepository, private val place_repository: PlaceRepository,
                            private val trip_place_repository: TripPlaceRepository) : ViewModel(){


    fun addTrip(trip: Trip): Long {
        return trip_repository.insertTrip(trip)
    }

    fun addPlace(place: Place) {
        place_repository.insert(place)
    }

    fun addTripPlace(trip_place: TripPlace) {
        trip_place_repository.insertTripPlace(trip_place)
    }

    fun existPlace(place: Place): Boolean {
        return place_repository.existsPlace(place)
    }

    fun getPlace(place: Place): Int {
        return place_repository.getPlaceId(place)
    }
    class ProgramTripViewModelFactory(
        private val trip_repository: TripRepository,
        private val place_repository: PlaceRepository,
        private val trip_place_repository: TripPlaceRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgramTripViewModel::class.java)) {
                return ProgramTripViewModel(trip_repository, place_repository, trip_place_repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
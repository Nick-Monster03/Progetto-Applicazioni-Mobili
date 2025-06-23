package com.example.myproject.ui.TripsStorical

import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    fun updateDescription(tripId: Int, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addDescription(tripId, desc)
        }
    }

    fun getAllTrips(): LiveData<List<Trip>> {
        return repository.getAllTrips()
    }

    fun  getPlacesById(tripId: Int): LiveData<List<Place>> {
        return repository.getPlacedById(tripId)
    }

    fun getDistanceById(tripId: Int): LiveData<Double> {
        val result = MediatorLiveData<Double>()
        val placesLiveData = repository.getPlacedById(tripId)

        result.addSource(placesLiveData) { places ->
            if (places != null && places.size >= 2) {
                result.value = calculateTotalDistance(places)
            } else {
                result.value = 0.0
            }
        }

        return result
    }



    fun filtraViaggi(tipo:String, dataStart: String, dataDestination: String): LiveData<List<Trip>> {
        var type: TripType? = null
        if(tipo == "EXCURSION")
            type = TripType.EXCURSION
        else if(tipo == "NO_PROGRAM")
            type = TripType.NO_PROGRAM
        else if(tipo == "JOURNEY")
            type = TripType.JOURNEY
        else if(tipo == "LOCAL")
            type = TripType.LOCAL

        return repository.getFilteredTrips(
            type,
            if (dataStart.isEmpty()) null else dataStart,
            if (dataDestination.isEmpty()) null else dataDestination
        )
    }

    private fun calculateTotalDistance(places: List<Place>): Double {
        if (places.size < 2){
            return 0.0
        }
        else
        {
            var totalDistance = 0.0
            for (i in 0 until places.size - 1) {
                val start = places[i]
                val end = places[i + 1]

                val results = FloatArray(1)
                Location.distanceBetween(
                    start.latitudine, start.longitudine,
                    end.latitudine, end.longitudine,
                    results
                )
                totalDistance += results[0]
            }
            return totalDistance / 1000.0 // metri → km
        }

    }






    class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
                return TripViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

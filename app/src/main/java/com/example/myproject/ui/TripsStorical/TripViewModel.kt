package com.example.myproject.ui.TripsStorical

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    //Prima per ogni viaggio era possibile visualizzare la descrizione
    //e modificarla, ora invece la descrizione viene inizializzata solo nei viaggi programmati
    //il metodo è stato comunque tenuto per future implementazioni
    /*fun updateDescription(tripId: Int, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addDescription(tripId, desc)
        }
    }*/

    fun getAllTrips(): LiveData<List<Trip>> {
        return repository.getAllEndedTrips()
    }

    fun  getPlacesById(tripId: Int): LiveData<List<Place>> {
        return repository.getPlacedById(tripId)
    }

    //funzione per ottenere la distanza totale percorsa durante un viaggio
    //usa MediatorLiveData per osservare le modifiche alla lista di Place
    fun getDistanceById(tripId: Int): LiveData<Double> {
        val result = MediatorLiveData<Double>()
        //val placesLiveData = repository.getPlacedById(tripId)
        val placesLiveData = getPlacesById(tripId)

        result.addSource(placesLiveData) { places ->
            if (places != null && places.size >= 2) {
                result.value = calculateTotalDistance(places)
            } else {
                result.value = 0.0
            }
        }

        return result
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun filtraViaggi(tipo:String, dataStart: String, dataDestination: String): LiveData<List<Trip>> {
        var type: TripType? = null
        if(tipo == "EXCURSION")
            type = TripType.EXCURSION
        else if(tipo == "JOURNEY")
            type = TripType.JOURNEY
        else if(tipo == "LOCAL")
            type = TripType.LOCAL

        return repository.getFilteredTrips(
            type,
            if (dataStart.isEmpty()) null else dataStart,
            if (dataDestination.isEmpty()) LocalDate.now().toString() else dataDestination
        )
    }

    //funzione per calcolare la distanza totale tra i luoghi di un viaggio
    //facendo somme parziali a coppie di places
    fun calculateTotalDistance(places: List<Place>): Double {
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

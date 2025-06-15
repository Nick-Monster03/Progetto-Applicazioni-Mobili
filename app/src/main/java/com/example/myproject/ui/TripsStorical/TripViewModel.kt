package com.example.myproject.ui.TripsStorical

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripType
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

    class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
                return TripViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

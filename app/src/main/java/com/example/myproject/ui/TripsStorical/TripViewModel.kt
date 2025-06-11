package com.example.myproject.ui.TripsStorical

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Trip
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

    class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
                return TripViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.example.myproject.ui.TripsStorical.Note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myproject.NoteRepository
import com.example.myproject.repository.PlaceRepository

class NoteViewModel(application: Application, private val tripId: Int) : AndroidViewModel(application) {

    private val noteRepo = NoteRepository(application)
    private val placeRepo = PlaceRepository(application)

    val notes = noteRepo.getNotesByTripId(tripId)
    val places = placeRepo.getPlacedByIdTrip(tripId)


    class Factory(private val app: Application, private val tripId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(app, tripId) as T
        }
    }
}

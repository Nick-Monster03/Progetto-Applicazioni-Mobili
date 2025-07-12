package com.example.myproject.ui.TripsStorical.Note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myproject.Repositories.NoteRepository
import com.example.myproject.Repositories.PlaceRepository

class NoteViewModel(application: Application, private val tripId: Int) : AndroidViewModel(application) {

    private val noteRepo = NoteRepository(application)
    //private val placeRepo = PlaceRepository(application)

    val notes = noteRepo.getNotesByTripId(tripId)// Recupera tutte le note associate a un determinato viaggio (tripId)
    //val places = placeRepo.getPlacedByIdTrip(tripId)

    /*
    Inizialmente si pensava di associare a ogni nota anche il luogo (Place) in cui è stata creata.
    Tuttavia, si è deciso di non implementare questa funzionalità per motivi sia estetici che logici:
    - Visivamente non era il massimo da vedere.
    - Dal punto di vista semantico, più note scritte in luoghi vicini ma distinti (es. via Farini e via Murri)
      sarebbero risultate tutte sotto lo stesso "luogo" (es. Bologna), perdendo il dettaglio reale.
    */


    class Factory(private val app: Application, private val tripId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(app, tripId) as T
        }
    }
}

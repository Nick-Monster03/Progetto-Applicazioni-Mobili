package com.example.myproject.ui.Photo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Place
import com.example.myproject.repository.PlaceRepository

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaceRepository(application)

    fun getAllPlaces(): LiveData<List<Place>> {
        return repository.getAllPlaces()
    }
}
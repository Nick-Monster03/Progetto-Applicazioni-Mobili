package com.example.myproject.ui.Photo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Repositories.PhotoRepository

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository = PhotoRepository(application)

    fun getPhotosForPlace(placeId: Int): LiveData<List<Photo>> {
        return repository.getPhotoAndPlaces(placeId)
    }

    fun getPhotosByTrip(tripId: Int): LiveData<List<Photo>> {
        return repository.getPhotoByTrip(tripId)
    }

    suspend fun getCountPhotosByTrip(tripId: Int): Int {
        //return repository.getPhotoByTrip(tripId).value?.size ?: 0
        return repository.getCountPhotosByTrip(tripId)
    }

}
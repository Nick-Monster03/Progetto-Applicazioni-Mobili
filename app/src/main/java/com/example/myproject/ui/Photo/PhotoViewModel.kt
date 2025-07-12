package com.example.myproject.ui.Photo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Repositories.PhotoRepository
import com.example.myproject.Repositories.TripRepository

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val photo_repository: PhotoRepository = PhotoRepository(application)
    private val trip_repository: TripRepository = TripRepository(application)

    //Inizialmente si era pensato di dividere le foto in base ai luoghi visistati (place)
    //non solo ai viaggi, il metodo è stato tenuto per future implemntazioni
    //ad esempio visualizzare una mappa con un marker per ogni foto
    fun getPhotosForPlace(placeId: Int): LiveData<List<Photo>> {
        return photo_repository.getPhotoAndPlaces(placeId)
    }

    fun getPhotosByTrip(tripId: Int): LiveData<List<Photo>> {
        return photo_repository.getPhotoByTrip(tripId)
    }

    suspend fun getCountPhotosByTrip(tripId: Int): Int {
        //return repository.getPhotoByTrip(tripId).value?.size ?: 0
        return photo_repository.getCountPhotosByTrip(tripId)
    }

    fun getAllTrips(): LiveData<List<Trip>> {
        return trip_repository.getAllEndedTrips()
    }

}
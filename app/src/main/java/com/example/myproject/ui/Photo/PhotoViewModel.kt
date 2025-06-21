package com.example.myproject.ui.Photo

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.repository.PhotoRepository

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository = PhotoRepository(application)

    fun getPhotosForPlace(placeId: Int): LiveData<List<Photo>> {
        return repository.getPhotoAndPlaces(placeId)
    }
}
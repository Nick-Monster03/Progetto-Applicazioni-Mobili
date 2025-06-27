package com.example.myproject.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Dao.PhotoDao
import com.example.myproject.Database.Dao.PlaceDao
import com.example.myproject.Database.TravelDatabase

class PhotoRepository(app: Application) {

    var photoDao: PhotoDao

    init {
        val db = TravelDatabase.getDatabase(app)
        photoDao = db.photoDao()
    }
    suspend fun insert(photo: Photo) {
        photoDao.insertPhoto(photo)
    }

    fun delete() {
        photoDao.deleteDone()
    }

    fun getPhotosByPlace(placeId: Int): LiveData<List<Photo>> {
        return photoDao.getPhotosByPlace(placeId)
    }

    fun getPhotoByTrip(tripId: Int): LiveData<List<Photo>> {
        return photoDao.getPhotosByTrip(tripId)
    }

    fun getPhotoAndPlaces(placeId: Int): LiveData<List<Photo>> {
        return photoDao.getPhotosAndPlaces(placeId)
    }

    suspend fun getCountPhotosByTrip(tripId: Int): Int {
        return photoDao.getCountPhotosByTrip(tripId)
    }

    fun existsPhoto(id_trip: Int, image_path: String): Boolean {
        return photoDao.existPhoto(id_trip, image_path) > 0
    }
}
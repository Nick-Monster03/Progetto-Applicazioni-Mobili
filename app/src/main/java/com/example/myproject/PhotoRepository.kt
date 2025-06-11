package com.example.myproject.repository

import androidx.lifecycle.LiveData
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Dao.PhotoDao

class PhotoRepository(private val photoDao: PhotoDao) {


    suspend fun insert(photo: Photo) {
        photoDao.insertPhoto(photo)
    }

    fun delete() {
        photoDao.deleteDone()
    }

    fun getPhotosByPlace(placeId: Int): LiveData<List<Photo>> {
        return photoDao.getPhotosByPlace(placeId)
    }
}
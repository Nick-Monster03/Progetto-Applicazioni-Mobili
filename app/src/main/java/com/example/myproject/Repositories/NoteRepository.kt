package com.example.myproject.Repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.NoteDao
import com.example.myproject.Database.Entities.Note
import com.example.myproject.Database.TravelDatabase

class NoteRepository(application: Application) {

    private val noteDao: NoteDao

    init {
        val db = TravelDatabase.getDatabase(application)
        noteDao = db.noteDao()
    }

    fun insert(note: Note) {
        noteDao.insert(note)
    }

    fun getNotesByTripId(tripId: Int): LiveData<List<Note>> {
        return noteDao.getNotesByTripId(tripId)
    }
}
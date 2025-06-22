package com.example.myproject

import android.app.Application
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

    fun getNotesByTripId(tripId: Int): List<Note> {
        return noteDao.getNotesByTripId(tripId)
    }
}
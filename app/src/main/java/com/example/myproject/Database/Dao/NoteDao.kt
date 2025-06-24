package com.example.myproject.Database.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myproject.Database.Entities.Note

@Dao
interface NoteDao {

    @Insert
    fun insert(note: Note)

    @Query("SELECT * FROM note_table WHERE id_trip = :tripId")
    fun getNotesByTripId(tripId: Int): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE id_trip = :tripId")
    fun getNotesByTripIdGroup(tripId: Int): LiveData<List<Note>>

}
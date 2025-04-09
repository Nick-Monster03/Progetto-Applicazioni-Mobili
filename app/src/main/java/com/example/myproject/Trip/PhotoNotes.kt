package com.example.myproject.Trip

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "PhotoNotes")
data class PhotoNotes(  @PrimaryKey(autoGenerate = true) val id: Int = 0,
                        val idTrip: Int,
                        val photoPath: String?,
                        val note: String?)

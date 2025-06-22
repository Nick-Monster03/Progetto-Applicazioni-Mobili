package com.example.myproject.Database.Entities


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_table",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["id_trip"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_place: Int,
    val id_trip: Int,
    val content: String,
    val timestamp: String
)
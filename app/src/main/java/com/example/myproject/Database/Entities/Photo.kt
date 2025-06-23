package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "photo_table",
    primaryKeys = ["id_place", "image_path"],
    foreignKeys = [
        ForeignKey(
            entity = Place::class,
            parentColumns = ["id"],
            childColumns = ["id_place"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)

class Photo(
    val id_place: Int,
    val id_trip: Int,
    val image_path: String,
    val timestamp: String

)
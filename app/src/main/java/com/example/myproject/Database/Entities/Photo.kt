package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "photo_table",
    primaryKeys = ["id_place", "photoBlob"],
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
    val photoBlob: ByteArray = ByteArray(0),
    val timestamp: Long

)
package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tripId", "placeId"],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Place::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
class TripPlace(
    val tripId: Int,
    val placeId: Int,
    val time_stamp: String
)
package com.example.myproject.Database.Entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monuments",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Monument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val isChecked: Boolean = false
)
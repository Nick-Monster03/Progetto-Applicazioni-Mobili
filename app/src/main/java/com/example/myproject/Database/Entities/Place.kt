package com.example.myproject.Database.Entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "place_table", indices = [Index(value = ["latitudine", "longitudine"], unique = true)])
class Place(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val latitudine: Double,
    val longitudine: Double,
    val name: String = ""
)



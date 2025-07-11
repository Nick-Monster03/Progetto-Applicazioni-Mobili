package com.example.myproject.Database.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myproject.Database.Entities.Monument

@Dao
interface MonumentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(monument: Monument): Long

    @Update
    suspend fun update(monument: Monument)

    @Delete
    suspend fun delete(monument: Monument)

    @Query("SELECT * FROM monuments ORDER BY name ASC")
    fun getAll(): LiveData<List<Monument>>

    @Query("SELECT * FROM monuments WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): LiveData<List<Monument>>

    @Query("SELECT * FROM monuments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Monument?

    @Query("SELECT * FROM monuments WHERE isChecked == 1 ORDER BY name ASC")
    fun getChecked(): LiveData<List<Monument>>

    @Query("UPDATE monuments SET isChecked = :checked WHERE id = :monumentId")
    suspend fun changeCheck(monumentId: Long, checked: Boolean)

    @Query("SELECT * FROM monuments WHERE isChecked == 0 ORDER BY name ASC")
    fun getUnchecked(): LiveData<List<Monument>>


}

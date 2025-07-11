package com.example.myproject.Repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.myproject.Database.Dao.MonumentDao
import com.example.myproject.Database.Dao.NoteDao
import com.example.myproject.Database.Entities.Monument
import com.example.myproject.Database.TravelDatabase

class MonumentRepository(application: Application) {
    private val monumentDao: MonumentDao


    init {
        val db = TravelDatabase.getDatabase(application)
        monumentDao = db.monumentDao()
    }

    fun getAllMonuments(): LiveData<List<Monument>> {
        return monumentDao.getAll()
    }

    fun searchMonuments(query: String): LiveData<List<Monument>> {
        if (query.isNullOrBlank()) {
            return getAllMonuments() // Return all monuments if query is empty
        }
      return monumentDao.search(query)
    }

    suspend fun insert(monument: Monument){
        monumentDao.insert(monument)
    }

    suspend fun update(monument: Monument) {
        monumentDao.update(monument)
    }

    suspend fun delete(monument: Monument) {
        monumentDao.delete(monument)
    }

    suspend fun getMonumentById(id: Long): Monument? {
        return monumentDao.getById(id)
    }

    fun getCheckedMonuments(): LiveData<List<Monument>> {
        return monumentDao.getChecked()
    }

    fun getUncheckedMonuments(): LiveData<List<Monument>> {
        return monumentDao.getUnchecked()
    }

    suspend fun changeCheck(monumentId: Long) {
        val monument = monumentDao.getById(monumentId)
        if(monument != null){
            if(monument.isChecked) {
                monumentDao.changeCheck(monumentId, false)//già ccheccato e quindi va uncheccked
            } else {
                monumentDao.changeCheck(monumentId, true)//non checkato e quindi va checkato
            }
        }
    }

}

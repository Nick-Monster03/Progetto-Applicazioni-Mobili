package com.example.myproject.ui.Monument

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Monument
import com.example.myproject.Repositories.MonumentRepository
import kotlinx.coroutines.launch

class MonumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = MonumentRepository(application)

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    val allMonuments: LiveData<List<Monument>> = repo.getAllMonuments()
    val filteredMonuments: LiveData<List<Monument>> = MediatorLiveData<List<Monument>>().apply {
        addSource(allMonuments) { list ->
            value = filterList(list, _searchQuery.value ?: "")
        }
        addSource(_searchQuery) { query ->
            value = filterList(allMonuments.value ?: emptyList(), query)
        }
    }

    // metodo che dato una lista di Monuments gli filtra in base al
    //matching tra una query (string) e il nome del monumento
    private fun filterList(list: List<Monument>, query: String): List<Monument> {
        if (query.isBlank())
            return list
        else
            return list.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }


    suspend fun insert(monument: Monument) {
        repo.insert(monument)
    }

    fun changeCheck(monumentId: Long){
        //Tutti i controlli sull' id del monumento sono già fatti nel repository
        viewModelScope.launch {
            repo.changeCheck(monumentId)
        }
    }

    fun getCheckedMonuments(): LiveData<List<Monument>> {
        return repo.getCheckedMonuments()
    }

    fun getUncheckedMonuments(): LiveData<List<Monument>> {
        return repo.getUncheckedMonuments()
    }

    class MonumentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MonumentViewModel::class.java)) {
                return MonumentViewModel(application) as T
            }
            throw IllegalArgumentException("Classe ViewModel non riconosciuta")
        }
    }

}

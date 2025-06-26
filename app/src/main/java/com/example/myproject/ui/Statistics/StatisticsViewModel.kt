package com.example.myproject.ui.Statistics

import android.app.Application
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.ui.TripsStorical.TripRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class StatisticsViewModel(private val repository: TripRepository) : ViewModel() {

    private val _periodFilter = MutableLiveData<PeriodFilter>(PeriodFilter.ALL)
    val periodFilter: LiveData<PeriodFilter> = _periodFilter
    val filteredTrips = MediatorLiveData<List<Trip>>()
    val allPlaces = MutableLiveData<List<Place>>()
    private val _heatmapPoints = MutableLiveData<List<LatLng>>()
    val heatmapPoints: LiveData<List<LatLng>> = _heatmapPoints

    init {
        filteredTrips.addSource(_periodFilter) { filter ->
            loadTripsForFilter(filter)
        }
    }

    private fun loadTripsForFilter(filter: PeriodFilter) {
        val fromDate = when (filter) {
            PeriodFilter.LAST_1_MONTH -> LocalDate.now().minusMonths(1)
            PeriodFilter.LAST_3_MONTHS -> LocalDate.now().minusMonths(3)
            PeriodFilter.LAST_8_MONTHS -> LocalDate.now().minusMonths(8)
            PeriodFilter.ALL -> null
        }?.format(DateTimeFormatter.ISO_DATE)

        val source = repository.getFilteredTrips(type = null, fromDate = fromDate, toDate = null)

        filteredTrips.addSource(source) { trips ->
            filteredTrips.value = trips
            filteredTrips.removeSource(source) // Evita di accumulare più sorgenti
        }
    }

    fun setFilter(filter: PeriodFilter) {
        _periodFilter.value = filter
    }

    fun getPlacesForTrip(tripId: Int): LiveData<List<Place>> {
        return repository.getPlacedById(tripId)
    }


/*
    fun loadAllPlaces() {

        val tripsLiveData = repository.getAllTrips()
        Log.d("Stat", "getAllTrips() chiamato")
        Log.e("Stat","STAMPA LAT LONGoooTTT ${tripsLiveData.value?.size ?: 0}")
        repository.getAllTrips().observeForever { tripsList ->
            val allPlacesList = mutableListOf<Place>()
            Log.e("Stat","STAMPA LAT LONGi")
            Log.e("Stat","STAMPA LAT LONGooo ${tripsList.size}")
            for (trip in tripsList) {
                repository.getPlacedById(trip.id).observeForever { tripPlaces ->
                    allPlacesList.addAll(tripPlaces)
                    allPlaces.postValue(allPlacesList)
                }
            }
        }
        Log.e("Stat","STAMPA LAT LONG")


    }

    fun computeDensities(trips: List<Trip>): ArrayList<LatLng> {
        val latLngList = ArrayList<LatLng>()
        for (trip in trips) {
            Log.d("Stat", "Processing trip with ID: ${trip.id}")
            val places = repository.getPlacedById(trip.id).value ?: emptyList()
            Log.d("Stat", "Found ${places.size} places for trip ID: ${trip.id}")
            for (place in places) {
                latLngList.add(LatLng(place.latitudine, place.longitudine))
            }
        }
        return latLngList
    }
*/

    fun calculateTotalDistance(places: List<Place>): Double {
        if (places.size < 2){
            return 0.0
        }
        else
        {
            var totalDistance = 0.0
            for (i in 0 until places.size - 1) {
                val start = places[i]
                val end = places[i + 1]

                val results = FloatArray(1)
                Location.distanceBetween(
                    start.latitudine, start.longitudine,
                    end.latitudine, end.longitudine,
                    results
                )
                totalDistance += results[0]
            }
            return totalDistance / 1000.0 // metri → km
        }

    }

    fun computeHeatmapPoints(trips: List<Trip>) {
        viewModelScope.launch {
            val allPoints = mutableListOf<LatLng>()
            for (trip in trips) {
                val placesLiveData = repository.getPlacedById(trip.id)
                val places = suspendUntilValue(placesLiveData) ?: continue
                allPoints.addAll(places.map { LatLng(it.latitudine, it.longitudine) })
            }
            _heatmapPoints.postValue(allPoints)
        }
    }
    private suspend fun <T> suspendUntilValue(liveData: LiveData<T>): T? {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val observer = object : androidx.lifecycle.Observer<T> {

                override fun onChanged(t: T) {
                    if (t != null) {
                        liveData.removeObserver(this)
                        continuation.resume(t, null)
                    }
                }


            }
            liveData.observeForever(observer)
        }
    }



    class StatisticsViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

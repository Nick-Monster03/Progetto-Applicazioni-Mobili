package com.example.myproject.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.TripPlaceRepository
import com.example.myproject.repository.PhotoRepository
import com.example.myproject.repository.PlaceRepository
import com.example.myproject.ui.TripsStorical.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val placeRepository = PlaceRepository(application)
    private val tripRepository = TripRepository(application)
    private val tripPlaceRepository = TripPlaceRepository(application)
    private val photoRepository = PhotoRepository(application)

    private val _location = MutableLiveData<LatLng?>()
    val location: LiveData<LatLng?> = _location

    private val _isTripRunning = MutableLiveData<Boolean>()
    val isTripRunning: LiveData<Boolean> = _isTripRunning

    private var tripId = -1

    fun updateLocation(loc: LatLng) {
        _location.value = loc
    }

    fun startTrip(place: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val trip = Trip(0, place.name, "", todayDate, "", "", TripType.NO_PROGRAM)

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val placeId = placeRepository.getPlaceId(place)
            tripId = tripRepository.insertTrip(trip).toInt()
            //tripId = tripRepository.getLastTrip()
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = tripId, placeId = placeId))
            _isTripRunning.postValue(true)
        }
    }

    fun stopTrip(place: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val placeId = placeRepository.getPlaceId(place)
            tripRepository.updateTrip(tripId, place.name, todayDate)
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = tripId, placeId = placeId))
            _isTripRunning.postValue(false)
        }
    }

    fun setTripRunning(isRunning: Boolean, tripId: Int) {
        _isTripRunning.value = isRunning
        this.tripId = tripId
    }

    fun addPhoto(id_place: Int, photoBlob: ByteArray, timestamp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var photo = Photo(id_place, photoBlob, timestamp)
            photoRepository.insert(photo)
        }

    }

    fun getPlaceIdByCordinates(lat: Double, lng: Double): Int {
        return placeRepository.getPlaceByCordinates(latitudine = lat, longitudine = lng)
    }

    class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
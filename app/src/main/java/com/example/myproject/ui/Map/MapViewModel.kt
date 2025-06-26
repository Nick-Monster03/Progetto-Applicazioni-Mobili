package com.example.myproject.ui.map

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Note
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.NoteRepository
import com.example.myproject.TripPlaceRepository
import com.example.myproject.repository.PhotoRepository
import com.example.myproject.repository.PlaceRepository
import com.example.myproject.ui.TripsStorical.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val placeRepository = PlaceRepository(application)
    private val tripRepository = TripRepository(application)
    private val tripPlaceRepository = TripPlaceRepository(application)
    private val photoRepository = PhotoRepository(application)
    private val noteRepository = NoteRepository(application)

    private val _location = MutableLiveData<LatLng?>()
    val location: LiveData<LatLng?> = _location

    private val _isTripRunning = MutableLiveData<Boolean>()
    val isTripRunning: LiveData<Boolean> = _isTripRunning

    private val _tripId = MutableLiveData<Int>()
    val tripId: LiveData<Int> get() = _tripId

    private val _placeId = MutableLiveData<Int>()
    val placeId: LiveData<Int> get() = _placeId

    fun updateLocation(loc: LatLng) {
        _location.value = loc
    }

    fun startTrip(place: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val trip = Trip(0, place.name, "", todayDate, "", "", TripType.NO_PROGRAM)

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val pId = placeRepository.getPlaceId(place)
            val tId = tripRepository.insertTrip(trip).toInt()
            val confirmedTripId = tripRepository.getLastTrip()
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = confirmedTripId, placeId = pId))

            _tripId.postValue(confirmedTripId)
            _placeId.postValue(pId)
            _isTripRunning.postValue(true)
           // Log.d("MapViewModel", "Trip started with ID: $tripId and $placeId" )
            _isTripRunning.postValue(true)
        }
    }

    fun stopTrip(place: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val pId = placeRepository.getPlaceId(place)
            val tId = _tripId.value ?: return@launch
            tripRepository.updateTrip(tId, place.name, todayDate)
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = tId, placeId = pId))
            _isTripRunning.postValue(false)
        }
    }

    fun setTripRunning(isRunning: Boolean, tripId: Int) {
        _isTripRunning.value = isRunning
        _tripId.value = tripId
    }

    fun addPhoto(id_place: Int, id_trip: Int,photo_path: String, timestamp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var photo = Photo(id_place, id_trip, photo_path, timestamp)
            photoRepository.insert(photo)
        }

    }

    fun getPlaceIdByCordinates(lat: Double, lng: Double): Int {
        return placeRepository.getPlaceByCordinates(latitudine = lat, longitudine = lng)
    }

    fun saveTripPoint(latitude: Double, longitude: Double) {
        if (isTripRunning.value == true) {
            val place = Place(
                id = 0,
                latitudine = latitude,
                longitudine = longitude,
                name = getCityFromCoordinates(application, latitude, longitude) ?: "Unknown Place"
            )
            placeRepository.insert(place)
            val pId = placeRepository.getPlaceByCordinates(place.latitudine, place.longitudine)
            val tId = _tripId.value ?: return

            tripPlaceRepository.insertTripPlace(
                TripPlace(tripId = tId, placeId = pId)
            )
            _placeId.postValue(pId)
        }
    }

    fun showNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Inserisci Nota")

        // Crea un EditText e aggiungilo al dialog
        val input = EditText(context)
        input.hint = "Scrivi qui la tua nota"
        builder.setView(input)

        // Configura i pulsanti del dialog
        builder.setPositiveButton("OK") { dialog, _ ->
            val pId = _placeId.value
            val tId = _tripId.value
            if (pId == null || tId == null) {
                Toast.makeText(context, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val note = input.text.toString()
            if (note.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val noteObject = Note(id_place = pId, id_trip = tId, content = note, timestamp = timestamp)
                    noteRepository.insert(noteObject)
                }
            } else {
                Toast.makeText(context, "Nota vuota non salvata", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Annulla") { dialog, _ ->
            dialog.cancel()
        }

        // Mostra il dialog
        builder.create().show()
    }

    fun existTripPlace(tripId: Int, placeId: Int): Boolean {
        return tripRepository.existsTripWithId(tripId) && placeRepository.existsPlaceById(placeId)
    }

    private fun getCityFromCoordinates(context: Context, lat: Double, lon: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val name = addresses?.firstOrNull()?.let {
                it.locality ?: it.subAdminArea ?: it.adminArea ?: "Sconosciuto"
            } ?: "Sconosciuto"
            name
        } catch (e: IOException) {
            e.printStackTrace()
            "Sconosciuto"
        }
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
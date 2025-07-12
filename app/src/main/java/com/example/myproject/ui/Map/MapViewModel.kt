package com.example.myproject.ui.map

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myproject.Database.Entities.Note
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.NoteRepository
import com.example.myproject.Repositories.TripPlaceRepository
import com.example.myproject.Repositories.PhotoRepository
import com.example.myproject.Repositories.PlaceRepository
import com.example.myproject.Repositories.TripRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapViewModel(application: Application) : AndroidViewModel(application) {

    // Inizializzazione dei repository per accedere al database
    private val placeRepository = PlaceRepository(application)
    private val tripRepository = TripRepository(application)
    private val tripPlaceRepository = TripPlaceRepository(application)
    private val photoRepository = PhotoRepository(application)
    private val noteRepository = NoteRepository(application)

    // Posizione corrente dell’utente
    private val _location = MutableLiveData<LatLng?>()
    val location: LiveData<LatLng?> = _location

    // Stato del viaggio (attivo o meno)
    private val _isTripRunning = MutableLiveData<Boolean>()
    val isTripRunning: LiveData<Boolean> = _isTripRunning

    // ID del viaggio corrente
    private val _tripId = MutableLiveData<Int>()
    val tripId: LiveData<Int> get() = _tripId

    // ID dell’ultimo luogo registrato
    private val _placeId = MutableLiveData<Int>()
    val placeId: LiveData<Int> get() = _placeId

    // Flag che indica se il servizio di tracking è attivo o meno ()
    private val _trackingRunningFlag = MutableLiveData<Boolean>() //mi aggiorna sempre se il tracking è attivo o meno o se ci sono aggiornamenti
    val trackingRunningFlag: LiveData<Boolean> get() = _trackingRunningFlag

    // LiveData che rappresenta la lista dei luoghi associati al viaggio corrente
    val tripPlaces: LiveData<List<Place>> = _tripId.switchMap { id ->
        placeRepository.getPlacedByIdTrip(id)
    }

    fun updateLocation(loc: LatLng) {
        _location.value = loc//Aggiorna la LiveData con la nuova posizione dell’utente
    }

    /*
      Avvia un nuovo viaggio: inserisce il luogo iniziale, crea il record Trip, registra il primo
      punto nel viaggio (TripPlace), aggiorna lo stato interno del ViewModel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startTrip(place: Place, type: TripType, callback: (tripId: Int) -> Unit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val trip = Trip(0, place.name, "", today, "", "", type)

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val pId = placeRepository.getPlaceId(place)
            val tId = tripRepository.insertTrip(trip).toInt()
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = tId, placeId = pId, time_stamp = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now())))

            _tripId.postValue(tId)
            _placeId.postValue(pId)
            _isTripRunning.postValue(true)
            callback(tId)
        }
    }

    /*
    Ferma il viaggio attivo: aggiorna la data di fine viaggio,
    registra l’ultima posizione e aggiorna lo stato interno
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun stopTrip(place: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            placeRepository.insert(place)
            val pId = placeRepository.getPlaceId(place)
            val tId = _tripId.value ?: return@launch //Se il tripId non è valido c'è un problema
            tripRepository.updateTrip(tId, place.name, todayDate)
            tripPlaceRepository.insertTripPlace(TripPlace(tripId = tId, placeId = pId, time_stamp = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now())))
            _isTripRunning.postValue(false)
            _tripId.postValue(-1)
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

    fun getTripPlaces(tripId: Int): LiveData<List<TripPlace>> {
        return tripPlaceRepository.getTripPlacesForTrip(tripId)
    }

    fun showNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Inserisci Nota")

        // Crea un EditText e aggiungilo al dialog
        val input = EditText(context)
        input.hint = "Scrivi qui la tua nota"
        builder.setView(input)

        // Configurazione dei pulsanti del dialog
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

    //Funzione che richiama la query per verificare che laa stessa foto non sia già stata aggiunta
    //allo stesso viaggio anche se in posti differenti così da evitare duplicati
    fun existPhoto(id_trip: Int, image_path: String): Boolean {
        return photoRepository.existsPhoto(id_trip, image_path)
    }

    //Metodo che prendendo la variabile tracking_running dalle sharedPrefererence
    // e mi dice se il tracciamento è ancora attivo
    fun refreshTrackingFlag(context: Context) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        _trackingRunningFlag.postValue(prefs.getBoolean("tracking_running", false))
    }

    //Ottiene la posizione corrente dell’utente e la converte in un oggetto Place,
    //includendo anche il nome della città (tramite Geocoder)
    fun getCurrentPlace(callback: (Place?) -> Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(application)

        // Verifica se i permessi per accedere alla posizione precisa sono stati concessi
        if (ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(application, "Permessi posizione non concessi", Toast.LENGTH_SHORT)
                .show()
            callback(null)
            return
        }

        fusedClient.lastLocation.addOnSuccessListener { location ->//questo metodo asincorno recupera l'ultima posizione
            if (location != null) {
                // Prova a ottenere il nome della città usando Geocoder, se fallisce usa Unknown (limite di Geocoder)
                val cityName = try {
                    Geocoder(application, Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1)
                        ?.firstOrNull()
                        ?.locality ?: "Unknown"
                } catch (e: IOException) {
                    e.printStackTrace()
                    "Unknown"
                }

                // usiamo la callback per ottenere l’oggetto Place in maniera sicura e asincrona,
                // cioè solo quando la posizione dell’utente è effettivamente disponibile
                callback(
                    Place(
                        id = 0,
                        latitudine = String.format(Locale.US, "%.4f", location.latitude).toDouble(),
                        longitudine = String.format(Locale.US, "%.4f", location.longitude).toDouble(),
                        name = cityName
                    )
                )
            } else {
                Toast.makeText(
                    application.baseContext,
                    "Impossibile ottenere la posizione attuale",
                    Toast.LENGTH_SHORT
                ).show()
                callback(null)
            }
        }.addOnFailureListener {
            Toast.makeText(
                application.baseContext,
                "Errore nell'ottenere la posizione",
                Toast.LENGTH_SHORT
            ).show()
            callback(null)
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
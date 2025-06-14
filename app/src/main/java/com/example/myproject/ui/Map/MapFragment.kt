package com.example.myproject.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Dao.PlaceDao
import com.example.myproject.Database.TravelDatabase
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Dao.TripDao
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Dao.TripPlaceDao
import com.example.myproject.Database.Entities.TripType
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var LOCATION = LatLng(44.496781, 11.356387)
    private var TRIP_ID = -1

    private lateinit var db: TravelDatabase
    private lateinit var placeDao: PlaceDao
    private lateinit var tripDao: TripDao
    private lateinit var trip_placeDao: TripPlaceDao
    private var isTripRunning: Boolean= false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_layout, container, false)
        //Log.e("TRIPID", TRIP_ID.toString())
        val btnStartAndStop = view.findViewById<Button>(R.id.StartAndStopButton)
        val textView = view.findViewById<TextView>(R.id.textView)
        val prefs = requireContext().getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)
        isTripRunning = prefs.getBoolean("trip_running", false)
        if (isTripRunning) {
            btnStartAndStop.text = "Stop"
            textView.text = "Interrompi Viaggio"
        } else {
            btnStartAndStop.text = "Start"
            textView.text = "Avvia il tuo Viaggio"
        }
        db = TravelDatabase.getDatabase(requireContext())
        placeDao = db.placeDao()
        tripDao = db.tripDao()
        trip_placeDao = db.tripPlaceDao()

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    // eventuale aggiornamento della UI
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1001
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    LOCATION = LatLng(location.latitude, location.longitude)
                    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                    mapFragment?.getMapAsync(this)
                } else {
                    Log.e("GPS_TEST", "Impossibile ottenere la posizione attuale")
                }
            }
        }


        btnStartAndStop.setOnClickListener {
            Toast.makeText(requireContext(), "Percorso avviato partendo dalla tua posizione attuale", Toast.LENGTH_LONG).show()
            val prefs = requireContext().getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            if (btnStartAndStop.text == "Stop") {
                isTripRunning = false
                editor.putBoolean("trip_running", false)
                editor.apply()
                btnStartAndStop.text = "Start"
                textView.text = "Avvia il tuo Viaggio"
                getCurrentPlace { place ->
                    place?.let { endTrip(it) }
                }
            } else {
                btnStartAndStop.text = "Stop"
                isTripRunning = true
                editor.putBoolean("trip_running", true)
                editor.apply()
                textView.text = "Interrompi Viaggio"
                getCurrentPlace { place ->
                    place?.let { startTrip(it) }
                }
            }
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val position = CameraPosition.Builder()
            .target(LatLng(LOCATION.latitude, LOCATION.longitude))
            .zoom(17f)
            .bearing(90f)
            .tilt(30f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isTripRunning) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun getCurrentPlace(callback: (Place?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = getCityFromCoordinates(requireContext(), location.latitude, location.longitude) ?: ""
                val currentPlace = Place(
                    id = 0,
                    latitudine = Math.round(location.latitude * 10.0) / 10.0,
                    longitudine = Math.round(location.longitude * 10.0) / 10.0,
                    name = cityName
                )
                callback(currentPlace)
            } else {
                callback(null)
            }
        }
    }

    private fun startTrip(currentPlace: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val trip = Trip(0, currentPlace.name, "", todayDate, "", "",TripType.NO_PROGRAM)

        CoroutineScope(Dispatchers.IO).launch {
            placeDao.insert(currentPlace)
            val placeId = placeDao.getPlacesByCoordinates(currentPlace.latitudine, currentPlace.longitudine)
            tripDao.insert(trip)
            val tripId = tripDao.getLastTripId()
            trip_placeDao.insert(TripPlace(tripId = tripId, placeId = placeId))
            TRIP_ID = tripId
        }
    }

    private fun endTrip(currentPlace: Place) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            placeDao.insert(currentPlace)
            val placeId = placeDao.getPlacesByCoordinates(currentPlace.latitudine, currentPlace.longitudine)
            tripDao.updateTrip(TRIP_ID, currentPlace.name, todayDate)
            val tripId = tripDao.getLastTripId()
            trip_placeDao.insert(TripPlace(tripId = tripId, placeId = placeId))
        }
    }

    private fun getCityFromCoordinates(context: Context, lat: Double, lon: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

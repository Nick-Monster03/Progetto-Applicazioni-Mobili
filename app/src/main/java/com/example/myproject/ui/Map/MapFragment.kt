package com.example.myproject.ui.map
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
import com.example.myproject.GeofenceReceiver
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var viewModel: MapViewModel

    private lateinit var btnStartAndStop: Button
    private lateinit var textView: TextView
    private lateinit var btn_photo: ImageButton
    private val GALLERY_REQUEST_CODE = 100
    private var geofenceAdded = false
    private var polyline: Polyline? = null
    private val polylinePoints = mutableListOf<LatLng>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_layout, container, false)
        val factory = MapViewModel.MapViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        btnStartAndStop = view.findViewById(R.id.StartAndStopButton)
        btn_photo = view.findViewById(R.id.button_add_photo)
        btn_photo.visibility = View.GONE
        textView = view.findViewById(R.id.textView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val latitude = Math.round(location.latitude * 100.0) / 100.0
                        val longitude = Math.round(location.longitude * 100.0) / 100.0
                        viewModel.saveTripPoint(latitude, longitude)
                    }
                    polylinePoints.add(latLng)
                    polyline?.points = polylinePoints
                    viewModel.updateLocation(latLng)
                    if (!geofenceAdded) {
                        addGeofence()
                        geofenceAdded = true
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    viewModel.updateLocation(latLng)
                    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                    mapFragment?.getMapAsync(this@MapFragment)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1001
            )
        }

        btn_photo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        btnStartAndStop.setOnClickListener {
            getCurrentPlace { place ->
                if (place != null) {
                    if (viewModel.isTripRunning.value == true) {
                        viewModel.stopTrip(place)
                    } else {
                        viewModel.startTrip(place)
                    }
                } else {
                    Toast.makeText(requireContext(), "Posizione non disponibile", Toast.LENGTH_SHORT).show()
                }
            }
        }
        /*DEBUG VISUALIZZA IL BOTTONE PER AGGIUNGERE PUNTI ALLA POLYLINE
        val btn_debug = view.findViewById<Button>(R.id.debug_button) // devi aggiungere il bottone nel layout!
        btn_debug.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val testPoints = listOf(
                    LatLng(44.4949, 11.3426), // Bologna
                    LatLng(44.4955, 11.3430),
                    LatLng(44.4960, 11.3435),
                    LatLng(44.4965, 11.3440)
                )
                testPoints.forEach {
                    polylinePoints.add(it)
                    polyline?.points = polylinePoints
                    delay(1000) // aspetta 1 secondo tra i punti
                }
            }
        }*/

        viewModel.isTripRunning.observe(viewLifecycleOwner, Observer { isRunning ->
            if (isRunning) {
                btn_photo.visibility = View.VISIBLE
                btnStartAndStop.text = "Stop"
                textView.text = "Interrompi Viaggio"
            } else {
                btn_photo.visibility = View.GONE
                btnStartAndStop.text = "Avvia"
                textView.text = "Avvia il tuo Viaggio"
            }
        })

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        viewModel.location.value?.let { loc ->
            val position = CameraPosition.Builder()
                .target(loc)
                .zoom(17f)
                .bearing(90f)
                .tilt(30f)
                .build()
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        }
        polyline = googleMap.addPolyline(
            PolylineOptions()
                .color(Color.BLUE)
                .width(8f)
                .addAll(polylinePoints) // vuoto all’inizio
        )
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateDistanceMeters(2000f).build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            addGeofence()
        }
    }

    private fun addGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        val geofence = Geofence.Builder()
            .setRequestId("travel_2km")
            .setCircularRegion(
                viewModel.location.value?.latitude ?: return,
                viewModel.location.value?.longitude ?: return,
                1000f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(requireContext(), GeofenceReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "chiamo addGeofence con lat=${viewModel.location.value?.latitude}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Errore geofence", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }
    }

    private fun getCurrentPlace(callback: (Place?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = getCityFromCoordinates(requireContext(), location.latitude, location.longitude) ?: ""
                val currentPlace = Place(
                    id = 0,
                    latitudine = Math.round(location.latitude * 100.0) / 100.0,
                    longitudine = Math.round(location.longitude * 100.0) / 100.0,
                    name = cityName
                )
                callback(currentPlace)
            } else {
                callback(null)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                var blob_image = inputStream?.readBytes()
                getCurrentPlace { place ->
                    if (place != null) {
                        if (viewModel.isTripRunning.value == true) {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                val id_place = viewModel.getPlaceIdByCordinates(place.latitudine, place.longitudine)

                                withContext(Dispatchers.Main) {
                                    if (id_place > 0 && blob_image != null && blob_image.isNotEmpty()) {
                                        viewModel.addPhoto(
                                            id_place,
                                            blob_image,
                                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                        )
                                        Toast.makeText(requireContext(), "Foto aggiunta con successo", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Errore nell'aggiunta della foto", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Avvia un viaggio per aggiungere foto", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Posizione non disponibile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
    /*DEBUG VISUALIZZA LA IMAGE VIEW IN ALTO (decommentare la Image view anche nel layout)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()

                if (imageBytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val imageView = view?.findViewById<ImageView>(R.id.imageView_photo)
                    imageView?.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(requireContext(), "Errore nella lettura dell'immagine", Toast.LENGTH_SHORT).show()
                }
            }
        }

        */






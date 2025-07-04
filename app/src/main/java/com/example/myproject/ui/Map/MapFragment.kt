package com.example.myproject.ui.map
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.GeofenceReceiver
import com.example.myproject.TrackingService
import com.example.myproject.ui.map.selectTripTypeDialog
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
import java.io.File
import java.io.FileOutputStream
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
    private lateinit var btn_add_note: ImageButton
    private val GALLERY_REQUEST_CODE = 100
    private var geofenceAdded = false
    private var polyline: Polyline? = null
    private val polylinePoints = mutableListOf<LatLng>()
    private lateinit var googleMap: GoogleMap
    private val REQUEST_LOCATION_PERMISSIONS = 1001
    private val REQUEST_BACKGROUND_LOCATION = 1002
    private val REQUEST_NOTIFICATIONS = 1003



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_layout, container, false)

        requestAllPermissions()
        val factory = MapViewModel.MapViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        btnStartAndStop = view.findViewById(R.id.StartAndStopButton)
        btn_photo = view.findViewById(R.id.button_add_photo)
        btn_add_note = view.findViewById(R.id.button_add_note)
        btn_photo.visibility = View.GONE
        btn_add_note.visibility = View.GONE
        textView = view.findViewById(R.id.textView)

        viewModel.trackingRunningFlag.observe(viewLifecycleOwner) { isRunning ->
            btn_photo.visibility = if (isRunning) View.VISIBLE else View.GONE
            btn_add_note.visibility = if (isRunning) View.VISIBLE else View.GONE
            btnStartAndStop.text = if (isRunning) "Stop" else "Avvia"
            textView.text = if (isRunning) "Interrompi Viaggio" else "Avvia il tuo Viaggio"
        }

        btnStartAndStop.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val isServiceRunning = prefs.getBoolean("tracking_running", false)
            // Se il tracking running è attivo (o flag true), fa solo STOP.
            // Se il tracking running è inattivo (flag false), fa solo START.
            getCurrentPlace { place ->
                if (place != null) {
                    if (viewModel.isTripRunning.value == true || isServiceRunning) {
                        viewModel.stopTrip(place)
                        Intent(requireContext(), TrackingService::class.java).apply {
                            action = TrackingService.ACTION_STOP
                        }.also { requireContext().startService(it) }
                        //requireContext().stopService(Intent(requireContext(), TrackingService::class.java))
                        prefs.edit().putBoolean("tracking_running", false).apply()
                        polyline?.remove()
                        polyline = googleMap.addPolyline(
                            PolylineOptions()
                                .color(Color.BLUE)
                                .width(15f)
                                .addAll(emptyList()))
                    } else {
                        requireContext().selectTripTypeDialog { selectedType ->
                            viewModel.startTrip(place, selectedType) { startedTripId ->
                                val intent = Intent(requireContext(), TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_START
                                    putExtra("tripId", startedTripId.toLong())
                                    putExtra("tripType", selectedType.name)
                                }

                                /*val stopIntent = Intent(requireContext(), TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_STOP
                                }

                                // Rimuovo il servizio se è già in esecuzione (se c'è ancora) per motivi si sicurezza
                                requireContext().startService(stopIntent)*/

                                //Poi START con un leggero delay per dare il tempo al Service di chiudersi
                                view?.postDelayed({
                                    requireContext().startService(intent)
                                    requireContext()
                                        .getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("tracking_running", true)
                                        .apply()
                                }, 800) // 800ms prima di poter aviare un altro viaggio per sicurezza


                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Posizione non disponibile", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //Aggiorno dinamicamente una polilinea sulla mappa ogni volta che cambia il contenuto del
        //LiveData tripPlaces. Se non ci sono luoghi (places è vuoto), la polilinea viene semplicemente resettata.
        viewModel.tripPlaces.observe(viewLifecycleOwner) { places ->
            if (::googleMap.isInitialized) {
                if (polyline == null) {
                    polyline = googleMap.addPolyline(
                        PolylineOptions()
                            .color(Color.BLUE)
                            .width(15f)
                    )
                }
                polylinePoints.clear()
                if (places.isNotEmpty()) { // se non è vuota significa che il viaggio è in corso
                    //se è vuota vuol dire che è appena iniziato e bypassareà l' if
                    polylinePoints.addAll(
                        places.map { LatLng(it.latitudine, it.longitudine) }
                    )
                }
                try {
                    polyline?.points = polylinePoints
                }catch (e: Exception) {}

            }
        }

        btn_photo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }
        btn_add_note.setOnClickListener { viewModel.showNoteDialog(requireContext()) }

        viewModel.isTripRunning.observe(viewLifecycleOwner) { isRunning ->
            btn_photo.visibility = if (isRunning) View.VISIBLE else View.GONE
            btn_add_note.visibility = if (isRunning) View.VISIBLE else View.GONE
            btnStartAndStop.text = if (isRunning) "Stop" else "Avvia"
            textView.text = if (isRunning) "Interrompi Viaggio" else "Avvia il tuo Viaggio"
        }

        viewModel.location.observe(viewLifecycleOwner) { loc ->
            if (loc != null && ::googleMap.isInitialized) {
                val position = CameraPosition.Builder()
                    .target(loc)
                    .zoom(17f)
                    .bearing(90f)
                    .tilt(30f)
                    .build()
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateLocation(LatLng(location.latitude, location.longitude))
                }
            }
        }

        return view

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        polyline = googleMap.addPolyline(
            PolylineOptions()
                .color(Color.BLUE)
                .width(15f)
                .addAll(polylinePoints)
        )

    }



/*
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
    }*/

    private fun getCurrentPlace(callback: (Place?) -> Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permessi posizione non concessi", Toast.LENGTH_SHORT).show()
            callback(null)
            return
        }

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = try {
                    Geocoder(requireContext(), Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1)
                        ?.firstOrNull()
                        ?.locality ?: "Unknown"
                } catch (e: IOException) {
                    e.printStackTrace()
                    "Unknown"
                }

                callback(
                    Place(
                        id = 0,
                        latitudine = String.format(Locale.US, "%.4f", location.latitude).toDouble(),
                        longitudine = String.format(Locale.US, "%.4f", location.longitude).toDouble(),
                        name = cityName
                    )
                )
            } else {
                Toast.makeText(requireContext(), "Impossibile ottenere la posizione attuale", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Errore nell'ottenere la posizione", Toast.LENGTH_SHORT).show()
            callback(null)
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
                val bitmap = BitmapFactory.decodeStream(inputStream)

                getCurrentPlace { place ->
                    if (place != null) {
                        if (viewModel.isTripRunning.value == true) {
                            val id_trip = viewModel.tripId.value
                            if (id_trip == null) {
                                Toast.makeText(requireContext(), "ID del viaggio non disponibile", Toast.LENGTH_SHORT).show()
                                return@getCurrentPlace
                            }

                            // Prendi sempre l'ultimo punto registrato
                            viewModel.getTripPlaces(id_trip).observeOnce(viewLifecycleOwner) { tripPlaces ->
                                if (tripPlaces.isNotEmpty()) {
                                    val orderedTripPlaces = tripPlaces.sortedBy { it.time_stamp.toLongOrNull() ?: 0L }
                                    val lastTripPlace = orderedTripPlaces.lastOrNull()
                                    val lastPlaceId = lastTripPlace?.placeId ?: -1

                                    if (lastPlaceId != -1) {
                                        savePhotoToPlace(lastPlaceId, id_trip, bitmap)
                                    } else {
                                        Toast.makeText(requireContext(), "Nessun punto valido trovato per associare la foto", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Nessun punto registrato ancora. Attendi un momento e riprova.", Toast.LENGTH_SHORT).show()
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

    // Metodo per il salvataggio foto
    private fun savePhotoToPlace(placeId: Int, tripId: Int, bitmap: Bitmap) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val filename = "photo_${System.currentTimeMillis()}.jpg"
            val photosDir = File(requireContext().filesDir, "photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val photoFile = File(photosDir, filename)
            FileOutputStream(photoFile).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
            }

            if (viewModel.existPhoto(tripId, photoFile.absolutePath)) {
                return@launch
            }

            withContext(Dispatchers.Main) {
                viewModel.addPhoto(
                    id_place = placeId,
                    id_trip = tripId,
                    photo_path = photoFile.absolutePath,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
                Toast.makeText(requireContext(), "Foto aggiunta con successo", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Questa estensione mi evita ogni volta che l' observer immetta ogni volta tutti i dati
    //del LiveData tripPlaces ottenuto con viewModel.getPlacesOfTrip(id_trip)
    //Se l' observer non venisse rimosso alla prima chiamata allora sarebbe effettuata l' operazione di
    //aggiunta di una foto ogni volta che nel LiveData viene aggiunto qualcosa, causando un loop infinito
    fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
        observe(owner, object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    private fun requestAllPermissions() {
        // Controllo e chiedo FINE e COARSE
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissions(notGranted.toTypedArray(), REQUEST_LOCATION_PERMISSIONS)
        } else {
            // Già concesse, passo al background
            requestBackgroundLocation()
        }
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION
                )
            } else {
                // Già concesso
                requestNotificationPermission()
            }
        } else {
            // Versioni più vecchie non hanno il background separato
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(requireContext(), "Permessi posizione concessi", Toast.LENGTH_SHORT).show()
                    requestBackgroundLocation()
                } else {
                    Toast.makeText(requireContext(), "Permessi posizione negati", Toast.LENGTH_LONG).show()
                }
            }

            REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(requireContext(), "Permesso background concesso", Toast.LENGTH_SHORT).show()
                    requestNotificationPermission()
                } else {
                    Toast.makeText(requireContext(), "Permesso background negato", Toast.LENGTH_LONG).show()
                }
            }

            REQUEST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(requireContext(), "Permesso notifiche concesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permesso notifiche negato", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTrackingFlag(requireContext())
        val isServiceRunning = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("tracking_running", false)

        if (!isServiceRunning) {
            viewModel.setTripRunning(false, -1)
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







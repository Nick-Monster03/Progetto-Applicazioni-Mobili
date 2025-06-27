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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_layout, container, false)
        val factory = MapViewModel.MapViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        btnStartAndStop = view.findViewById(R.id.StartAndStopButton)
        btn_photo = view.findViewById(R.id.button_add_photo)
        btn_add_note = view.findViewById(R.id.button_add_note)
        btn_photo.visibility = View.GONE
        btn_add_note.visibility = View.GONE
        textView = view.findViewById(R.id.textView)

        btnStartAndStop.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val isServiceRunning = prefs.getBoolean("tracking_running", false)

            getCurrentPlace { place ->
                if (place != null) {
                    if (viewModel.isTripRunning.value == true || isServiceRunning) {
                        viewModel.stopTrip(place)
                        requireContext().stopService(Intent(requireContext(), TrackingService::class.java))
                        prefs.edit().putBoolean("tracking_running", false).apply()
                    } else {
                        requireContext().selectTripTypeDialog { selectedType ->
                            viewModel.startTrip(place, selectedType) { startedTripId ->
                                val intent = Intent(requireContext(), TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_START
                                    putExtra("tripId", startedTripId.toLong())
                                }

                                requireContext().stopService(Intent(requireContext(), TrackingService::class.java))
                                requireContext().startService(intent)
                                prefs.edit().putBoolean("tracking_running", true).apply()


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
                polylinePoints.clear()
                if (places.isNotEmpty()) { // se non è vuota significa che il viaggio è in corso
                    //se è vuota vuol dire che è appena iniziato e bypassareà l' if
                    polylinePoints.addAll(
                        places.map { LatLng(it.latitudine, it.longitudine) }
                    )
                }
                polyline?.points = polylinePoints
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

                            // Osserviamo i dati sul Main Thread
                            viewModel.getPlacesOfTrip(id_trip).observeOnce(viewLifecycleOwner) { tripPlaces ->
                                Log.e("MapFragment", "Trip Places: $id_trip")
                                Log.e("MapFragment", "Trip Places Size: ${tripPlaces.size}")
                                if (tripPlaces.isNullOrEmpty()) {
                                    Toast.makeText(requireContext(), "Nessun luogo disponibile per associare la foto", Toast.LENGTH_SHORT).show()
                                    return@observeOnce
                                }

                                val id_place = viewModel.getClosestPlaceId(
                                    place.latitudine,
                                    place.longitudine,
                                    tripPlaces
                                )

                                //Troviamo l' ID del nostro luogo attuale dato che sarà sicuramente registrato nel db
                                /*val lat = String.format(Locale.US, "%.4f", place.latitudine).toDouble()
                                val long = String.format(Locale.US, "%.4f", place.longitudine).toDouble()
                                val id_place = viewModel.getPlaceIdByCordinates(lat, long)*/

                                tripPlaces.forEach { tripPlace ->
                                    Log.e("MapFragment", "TripPlace - Latitudine: ${tripPlace.latitudine}, Longitudine: ${tripPlace.longitudine}")
                                }

                                Log.e("MapFragment", "ID Place: $id_place")

                                if (id_place == -1) {
                                    Toast.makeText(requireContext(), "Nessun luogo vicino trovato per questa foto", Toast.LENGTH_SHORT).show()
                                    return@observeOnce
                                }

                                // Adesso lanciamo la Coroutine per salvare la foto
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    val filename = "photo_${System.currentTimeMillis()}.jpg"
                                    val photosDir = File(requireContext().filesDir, "photos")
                                    if (!photosDir.exists()) photosDir.mkdirs()

                                    val photoFile = File(photosDir, filename)
                                    FileOutputStream(photoFile).use {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
                                    }

                                    if(viewModel.existPhoto(id_trip, photoFile.absolutePath)) {
                                        return@launch
                                    }
                                    withContext(Dispatchers.Main) {
                                        viewModel.addPhoto(
                                            id_place = id_place,
                                            id_trip = id_trip,
                                            photo_path = photoFile.absolutePath,
                                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                        )
                                        Toast.makeText(requireContext(), "Foto aggiunta con successo", Toast.LENGTH_SHORT).show()
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






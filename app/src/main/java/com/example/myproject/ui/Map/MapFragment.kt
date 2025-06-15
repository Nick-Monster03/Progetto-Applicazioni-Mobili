package com.example.myproject.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_layout, container, false)
        val factory = MapViewModel.MapViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        btnStartAndStop = view.findViewById(R.id.StartAndStopButton)
        btn_photo = view.findViewById(R.id.button_add_photo)
        textView = view.findViewById(R.id.textView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    viewModel.updateLocation(latLng)
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
        //DEBUG
        //btn_photo.setOnClickListener({Toast.makeText(requireContext(), "Aggiungi nuova foto dalla galleria", Toast.LENGTH_SHORT).show()})
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

        viewModel.isTripRunning.observe(viewLifecycleOwner, Observer { isRunning ->
            if (isRunning) {
                btnStartAndStop.text = "Stop"
                textView.text = "Interrompi Viaggio"
                //btn_photo.visibility = View.VISIBLE
            } else {
                btnStartAndStop.text = "Start"
                textView.text = "Avvia il tuo Viaggio"
                //btn_photo.visibility = View.GONE
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
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
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
                            Toast.makeText(requireContext(), "Avvia un viaggio per aggiungere foto", Toast.LENGTH_SHORT).show(
                            )
                        }
                    } else {
                        Toast.makeText(requireContext(), "Posizione non disponibile", Toast.LENGTH_SHORT).show()
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




}

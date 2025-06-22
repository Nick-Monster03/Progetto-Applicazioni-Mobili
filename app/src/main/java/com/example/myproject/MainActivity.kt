package com.example.myproject

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myProject.R
import com.example.myproject.Notification.InactivityWorker
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {



    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inizializza elementi
        drawerLayout = findViewById(R.id.main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Toolbar + toggle drawer
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Collega NavigationUI con il Drawer e il NavController
        NavigationUI.setupWithNavController(navView, navController)

        navView.setNavigationItemSelectedListener(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Inizializza il PeriodicWorkRequest per il controllo dell'inattività
        val periodicRequest = PeriodicWorkRequestBuilder<InactivityWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "trip_inactivity_check",
            //ExistingPeriodicWorkPolicy.KEEP,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicRequest
        )
        /*DEBUG NOTIFICATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        val testRequest = OneTimeWorkRequestBuilder<InactivityWorker>().build()
        WorkManager.getInstance(this).enqueue(testRequest)
        */
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(item, navController)
        drawerLayout.closeDrawer(GravityCompat.START)
        return handled
    }



}



/*
     val btnStartAndStop = findViewById<Button>(R.id.StartAndStopButton)
        btnStartAndStop.setOnClickListener {
            //getCurrentPosition()
            Toast.makeText(
                this,
                "Percorso avviato partendo dalla tua posizione attuale",
                Toast.LENGTH_LONG
            ).show()
            var currentPlace: Place;
            if(btnStartAndStop.text == "Stop") {
                btnStartAndStop.text = "Start"
                val text_view = findViewById<TextView>(R.id.textView)
                text_view.text = "Avvia il tuo Viaggio"
                getCurrentPlace { place ->
                    if (place != null) {
                        currentPlace = place
                        endTrip(currentPlace)
                    } else {
                        Log.e("GPS", "Posizione non disponibile")
                    }
                }
            }else{
                btnStartAndStop.text = "Stop"
                val text_view = findViewById<TextView>(R.id.textView)
                text_view.text = "Interrompi Viaggio"
                getCurrentPlace { place ->
                    if (place != null) {
                        currentPlace = place
                        startTrip(currentPlace)
                    } else {
                        Log.e("GPS", "Posizione non disponibile")
                    }
                }
            }
        }
        geofencingClient = LocationServices.getGeofencingClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    // Update UI with location data
                    //Log.d("LOCATION UPDATE", "update")
                }
            }
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                LOCATION = LatLng(location.latitude, location.longitude)
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                mapFragment?.getMapAsync(this)
            } else {
                Log.e("GPS_TEST", "Impossibile ottenere la posizione attuale")
            }
        }
    }

    fun getCurrentPlace(callback: (Place?) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = getCityFromCoordinates(this, location.latitude, location.longitude) ?: ""
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

    fun startTrip(currentPlace: Place) {
        db = TravelDatabase.getDatabase(applicationContext)
        placeDao = db.placeDao()
        tripDao = db.tripDao()
        trip_placeDao = db.tripPlaceDao()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val trip = Trip(
            id = 0,
            start = currentPlace.name,
            destination = "",
            startDate = todayDate,
            endDate = "",
            type = TripType.NO_PROGRAM
        )

        CoroutineScope(Dispatchers.IO).launch {
            placeDao.insert(currentPlace)
            val placeId = placeDao.getPlacesByCoordinates(
                currentPlace.latitudine,
                currentPlace.longitudine
            )
            tripDao.insert(trip)
            val tripId = tripDao.getLastTripId()

            trip_placeDao.insert(TripPlace(
                tripId = tripId,
                placeId = placeId
            ))

            TRIP_ID = tripId
        }
    }

    fun endTrip(currentPlace: Place){
        db = TravelDatabase.getDatabase(applicationContext)
        placeDao = db.placeDao()
        tripDao = db.tripDao()
        trip_placeDao = db.tripPlaceDao()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            placeDao.insert(currentPlace)
            val placeId = placeDao.getPlacesByCoordinates(
                currentPlace.latitudine,
                currentPlace.longitudine
            )
            tripDao.updateTrip(
                TRIP_ID,
                currentPlace.name,
                todayDate
            )
            val tripId = tripDao.getLastTripId()

            trip_placeDao.insert(TripPlace(
                tripId = tripId,
                placeId = placeId
            ))
        }
    }

    fun getCityFromCoordinates(context: Context, lat: Double, lon: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val position = CameraPosition.Builder()
            .target(LatLng(LOCATION.latitude, LOCATION.longitude))
            .zoom(17f)
            .bearing(90f)
            .tilt(30f)
            .build()
        googleMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(position)
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.nav_add_description -> DescriptionFragment()
            R.id.nav_statistics -> StatisticsFragment()
            R.id.nav_add_photo -> AddPhotoFragment()
            R.id.nav_program_trip -> ProgramTripFragment()
            else -> null
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
        Toast.makeText(this, "Click su ${item.title}", Toast.LENGTH_SHORT).show()
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}*/
package com.example.myproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationRequest
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.PlaceRepository
import com.example.myproject.Repositories.TripPlaceRepository
import com.example.myproject.Repositories.TripRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class TrackingService : LifecycleService() {

    companion object {
        const val ACTION_START = "START_TRACKING"
        const val ACTION_STOP = "STOP_TRACKING"
        const val CHANNEL_ID = "tracking_channel"
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private var tripId: Long = -1L
    private var lastLocation: Location? = null
    private var isTracking = false
    private lateinit var tripType: TripType



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                if (isTracking) {
                    return START_STICKY
                }
                tripId = intent.getLongExtra("tripId", -1)
                //Se non dovesse selezionare bene il tipo di viaggio di default sarà un LOCAL
                val tripTypeString = intent.getStringExtra("tripType") ?: "LOCAL"
                tripType = TripType.valueOf(tripTypeString)
                if (tripId <= 0) {
                    stopForeground(true)
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForeground(1, buildNotification())
                startLocationUpdates()
                startMidnightChecker()
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(true)
                stopSelf()
                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("tracking_running", false)
                    .apply()
            }
        }
        return START_STICKY
    }

    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationResult(result: LocationResult) {
            if (!isTracking) return
            result.locations.forEach { location ->
                // Filtriamo i punti di accuratezza scarsa
                if (location.accuracy > 25f) {
                    //Log.w("TrackingService", "Ignorato punto con accuratezza scarsa (${location.accuracy}m)")
                    return@forEach
                }

                //Filtro vecchiaia (>60 sec) così da evitare che il servizio gps non trovando punti al momento
                //della geolocalizzasizone prenda dei vecchi punti in cache
                val ageMillis = System.currentTimeMillis() - location.time
                if (ageMillis > 60_000L) {
                    // Log.w("TrackingService", "Ignorato punto vecchio di ${ageMillis/1000} sec")
                    return@forEach
                }

                // 3) Prosegui a salvare la posizione
                val latitudine = String.format(Locale.US, "%.4f", location.latitude).toDouble()
                val longitudine = String.format(Locale.US, "%.4f", location.longitude).toDouble()
                val place = Place(
                    id = 0,
                    latitudine = latitudine,
                    longitudine = longitudine,
                    name = getCityFromCoordinates(this@TrackingService, latitudine, longitudine) ?: "Unknown",
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val placeRepo = PlaceRepository(application)
                    val tripPlaceRepo = TripPlaceRepository(application)

                    placeRepo.insert(place)
                    val placeId = placeRepo.getPlaceByCordinates(place.latitudine, place.longitudine)
                    tripPlaceRepo.insertTripPlace(TripPlace(tripId = tripId.toInt(), placeId = placeId, time_stamp = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now()).toString()))
                }
            }
        }
    }

    private fun startLocationUpdates() {
        isTracking = true

        val request = when (tripType) {
            TripType.EXCURSION -> {
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    30_000L // intervallo preferito 30 sec
                ).apply {
                    setMinUpdateIntervalMillis(15_000L) // minimo 15 sec
                    setMinUpdateDistanceMeters(50f)     // min distanza 50 m
                }.build()
            }
            TripType.JOURNEY -> {
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    60_000L // intervallo preferito 60 sec
                ).apply {
                    setMinUpdateIntervalMillis(30_000L) // minimo 30 sec
                    setMinUpdateDistanceMeters(150f)    // min distanza 150 m
                }.build()
            }
            else -> { // TripType.LOCAL o default
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    20_000L // intervallo preferito 20 sec
                ).apply {
                    setMinUpdateIntervalMillis(10_000L) // minimo 10 sec
                    setMinUpdateDistanceMeters(25f)     // min distanza 25 m
                }.build()
            }
        }

        //Come funziuona la richiesta:
        //È passato almeno fastestInterval dal precedente aggiornamento.
        //Il dispositivo si è spostato almeno di smallestDisplacement metri dal precedente aggiornamento.
        //È passato interval dall'ultimo aggiornamento se non c'è stato spostamento sufficiente
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }

    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }

    private fun buildNotification(): Notification {
        var msg = ""
        if(tripType == TripType.EXCURSION)
            msg = "una gita fuori porta"
        else if(tripType == TripType.JOURNEY)
            msg = "una vacanza di più giorni"
        else  //default
            msg = "una passeggiata"
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking in corso")
            .setContentText("Registrazione viaggio attiva. Strai tracciando " + msg)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentIntent(pendingIntent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tracking Viaggi",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
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

    //Essendo che Excursion e Local durano solo un giorno non di più il tracking sarà interrotto
    //e la destinazione del viaggio corrente sarà il luogo in cui attiualemnte si trova l' utente, se
    //il cellulare è spento allora sarà località sconosciuta
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMidnightChecker() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isTracking && (tripType == TripType.LOCAL || tripType == TripType.EXCURSION)) {
                val now = java.time.LocalTime.now()
                if (now.hour == 23 && now.minute == 59) {
                    stopLocationUpdates()
                    stopForeground(true)
                    getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("tracking_running", false)
                        .apply()
                    CoroutineScope(Dispatchers.IO).launch {
                        val placeRepo = PlaceRepository(application)
                        val tripRepo = TripRepository(application)
                        val tripPlaceRepo = TripPlaceRepository(application)

                        val location = lastLocation
                        val place = if (location != null) {
                            Place(
                                id = 0,
                                latitudine = String.format(Locale.US, "%.4f", location.latitude).toDouble(),
                                longitudine = String.format(Locale.US, "%.4f", location.longitude).toDouble(),
                                name = getCityFromCoordinates(this@TrackingService, location.latitude, location.longitude) ?: "Unknown"
                            )
                        } else {
                            Place(
                                id = 0,
                                latitudine = 0.0,
                                longitudine = 0.0,
                                name = "Località sconosciuta"
                            )
                        }

                        // Salva la place
                        placeRepo.insert(place)
                        val pId = placeRepo.getPlaceByCordinates(place.latitudine, place.longitudine)
                        tripRepo.updateTrip(
                            tripId.toInt(),
                            place.name,
                            java.time.LocalDate.now().toString()
                        )
                        tripPlaceRepo.insertTripPlace(
                            TripPlace(
                                tripId = tripId.toInt(),
                                placeId = pId,
                                time_stamp = java.time.LocalDate.now().toString()
                            )
                        )
                    }
                    stopSelf()
                    break
                }
                delay(60_000L) // Controlla ogni minuto
            }
        }
    }


    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()

    }
}


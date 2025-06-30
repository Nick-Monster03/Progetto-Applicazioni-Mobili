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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleService
import com.example.myProject.R
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.repository.PlaceRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                if (isTracking) {
                    return START_STICKY
                }
                tripId = intent.getLongExtra("tripId", -1)
                startForeground(1, buildNotification())
                startLocationUpdates()
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationResult(result: LocationResult) {
            if (!isTracking) return
            result.locations.forEach { location ->
                // 1) Filtra accuratezza scarsa
                if (location.accuracy > 25f) {
                    //Log.w("TrackingService", "Ignorato punto con accuratezza scarsa (${location.accuracy}m)")
                    return@forEach
                }

                // 2) Se abbiamo un punto precedente, controlla la distanza massima
                if (lastLocation != null) {
                    val distance = lastLocation!!.distanceTo(location)
                    /*if (distance > 500f) {
                       // Log.w("TrackingService", "Ignorato punto troppo distante dal precedente (${distance}m)")
                        //Toast.makeText(requireContext(application), "Punto ignoRATO PERCHè TROPPO DISTANTE")
                        return@forEach
                    }*/

                    // Aggiorna lastLocation solo se il punto è valido
                    lastLocation = location

                } else {
                    // Se è il primo punto, accettalo e imposta lastLocation
                    lastLocation = location
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
                    tripPlaceRepo.insertTripPlace(TripPlace(tripId = tripId.toInt(), placeId = placeId, time_stamp = java.time.LocalDate.now().toString()))
                }
            }
        }
    }

    private fun startLocationUpdates() {
        isTracking = true
        val request = LocationRequest.create().apply {
            interval = 10_000L
            fastestInterval = 5_000L
            smallestDisplacement = 100f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }

    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking in corso")
            .setContentText("Registrazione viaggio attiva")
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
}


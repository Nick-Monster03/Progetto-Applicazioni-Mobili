package com.example.myproject

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myProject.R
import com.example.myproject.Notification.InactivityWorker
import com.example.myproject.Notification.ProgramTripWorker
import com.example.myproject.Notification.TransictionReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.material.navigation.NavigationView
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {



    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private val REQUEST_LOCATION_PERMISSIONS = 1001
    private val REQUEST_BACKGROUND_LOCATION = 1002
    private val REQUEST_NOTIFICATIONS = 1003
    private val REQUEST_ACTIVITY_RECOGNITION = 1004


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        //Richiesta dei permessi necessari
        requestAllPermissions()

        // Inizializza il PeriodicWorkRequest per il controllo dell'inattività
        //DEBUG ogni 5 minuti val periodicRequest = PeriodicWorkRequestBuilder<InactivityWorker>(5, TimeUnit.MINUTES).build()
        val periodicRequest = PeriodicWorkRequestBuilder<InactivityWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "trip_inactivity_check",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicRequest
        )

        val currentDateTime = Calendar.getInstance()
        val nextMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(currentDateTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val initialDelay = nextMidnight.timeInMillis - currentDateTime.timeInMillis
        val dailyRequest = PeriodicWorkRequestBuilder<ProgramTripWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_trip_check",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            dailyRequest
        )
        /*DEBUG NOTIFICATIONs
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

        val testRequest = androidx.work.OneTimeWorkRequestBuilder<ProgramTripWorker>()
            .build()
        WorkManager.getInstance(this).enqueue(testRequest)
        */
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(item, navController)
        drawerLayout.closeDrawer(GravityCompat.START)
        return handled
    }

    private fun startActivityRecognition() {
        val client = ActivityRecognition.getClient(this)

        val request = ActivityTransitionRequest(
            listOf(
                // WALKING
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                // RUNNING
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                // ON_BICYCLE
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.ON_BICYCLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.ON_BICYCLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                // IN_VEHICLE
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
        )

        val intent = Intent(this, TransictionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permesso riconoscimento attività mancante", Toast.LENGTH_SHORT).show()
            return
        }

        client.requestActivityTransitionUpdates(request, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Monitoraggio attività avviato", Toast.LENGTH_SHORT).show()
                Log.d("ACTIVITY_RECOGNITION", "Richiesta avviata con ENTER + EXIT")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Errore nell'avvio del monitoraggio", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    //Metodi per ottenere tutti i permessi rischiesti per il corretto funzionamento dell' applicazione
    private fun requestAllPermissions() {
        // Controllo e chiedo FINE e COARSE
        Log.d("PERMISSION", "requestAllPermissions() chiamato")
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val locationNotGranted = locationPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (locationNotGranted.isNotEmpty()) {
            Log.d("PERMISSION", "Richiedo permessi: ${locationNotGranted.joinToString()}")
            requestPermissions(locationNotGranted.toTypedArray(), REQUEST_LOCATION_PERMISSIONS)
        } else {
            Log.d("PERMISSION", "Permessi posizione già concessi, richiedo background location")
            requestBackgroundLocation()
        }
        //DEBUG
        //Log.d("PERMISSIONS", "Sto per chiedere: ${notGranted.joinToString()}")

    }

    private fun requestBackgroundLocation() {
        Log.d("PERMISSION", "requestBackgroundLocation() chiamato")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSION", "Richiedo ACCESS_BACKGROUND_LOCATION")
                requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION)
            } else {
                Log.d("PERMISSION", "ACCESS_BACKGROUND_LOCATION già concesso")
                requestNotificationPermission()
            }
        } else {
            Log.d("PERMISSION", "Versione < Q, salto background location")
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            }else{
                requestActivityRecognition()
            }
        }else{
            requestActivityRecognition()
        }
    }

    private fun requestActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("PERMISSION", "Richiedo ACTIVITY_RECOGNITION")
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION
                )
            } else {
                Log.d("PERMISSION", "ACTIVITY_RECOGNITION già concesso")
                startActivityRecognition()
            }
        } else {
            // Versioni più vecchie non richiedono permesso
            startActivityRecognition()
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
                    Log.d("PERMISSION", "ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION: CONCESSI")
                    Toast.makeText(this, "Permessi posizione concessi", Toast.LENGTH_SHORT).show()
                    requestBackgroundLocation()
                } else {
                    Log.d("PERMISSION", "ACCESS_FINE_LOCATION e/o ACCESS_COARSE_LOCATION: NEGATI")
                    Toast.makeText(this, "Permessi posizione negati", Toast.LENGTH_LONG).show()
                }
            }

            REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("PERMISSION", "ACCESS_BACKGROUND_LOCATION: CONCESSO")
                    Toast.makeText(this, "Permesso background concesso", Toast.LENGTH_SHORT).show()
                    requestNotificationPermission()
                } else {
                    Log.d("PERMISSION", "ACCESS_BACKGROUND_LOCATION: NEGATO")
                    Toast.makeText(this, "Permesso background negato", Toast.LENGTH_LONG).show()
                }
            }

            REQUEST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("PERMISSION", "POST_NOTIFICATIONS: CONCESSO")
                    Toast.makeText(this, "Permesso notifiche concesso", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("PERMISSION", "POST_NOTIFICATIONS: NEGATO")
                    Toast.makeText(this, "Permesso notifiche negato", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("PERMISSION", "ACTIVITY_RECOGNITION: CONCESSO")
                    Toast.makeText(this, "Permesso riconoscimento attività concesso", Toast.LENGTH_SHORT).show()
                    startActivityRecognition()
                } else {
                    Log.d("PERMISSION", "ACTIVITY_RECOGNITION: NEGATO")
                    Toast.makeText(this, "Permesso riconoscimento attività negato", Toast.LENGTH_LONG).show()
                }
            }
        }
    }





}




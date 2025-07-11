package com.example.myproject.Receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myProject.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class MonumentGeofenceReceiver : BroadcastReceiver() {

    /*override fun onReceive(context: Context?, intent: Intent?) {
        val now = System.currentTimeMillis()

        fun logPersistently(message: String) {
            Log.d("GEOFENCE_RECEIVER", message)
            // Salva nel file interno
            context?.openFileOutput("geofence_log.txt", Context.MODE_APPEND)?.use {
                it.write("[$now] $message\n".toByteArray())
            }
            // Mostra notifica
            context?.let { showNotification(it, message) }
        }

        if (context == null || intent == null) {
            logPersistently("❌ Context or Intent is null. Aborting.")
            return
        }

        val isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        logPersistently("✅ onReceive triggered — Play Services availability: $isAvailable")

        logPersistently("Intent action: ${intent.action}")
        logPersistently("Intent extras keys: ${intent.extras?.keySet()?.joinToString() ?: "Nessuna"}")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent?.let {
                GeofenceStatusCodes.getStatusCodeString(it.errorCode)
            } ?: "GeofencingEvent is null"
            logPersistently("❌ Error in geofencing event: $errorMessage")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val ids = triggeringGeofences?.joinToString(", ") { it.requestId }

        val message = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "➡️ Entrato in zona: $ids"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "⬅️ Uscito dalla zona: $ids"
            else -> "❓ Transizione sconosciuta: $transitionType ($ids)"
        }

        logPersistently(message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }*/

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            //DEBUG
            // Log.e("GEOFENCE_RECEIVER", "Context o Intent null. Abort.")
            return
        }

        //DEBUG:
        // Log.d("GEOFENCE_RECEIVER", "onReceive triggered — Play Services: ${
        //    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        //}")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            //DEBUG:
            // val error = geofencingEvent?.errorCode?.let {
            //    GeofenceStatusCodes.getStatusCodeString(it)
            //} ?: "GeofencingEvent is null"
            //Log.e("GEOFENCE_RECEIVER", "❌ Errore geofence: $error")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val ids = triggeringGeofences?.joinToString(", ") { it.requestId }

        val message = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Sei vicino ad un munumento contrassegnato: $ids"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Ti stai allontanando dalla zona di: $ids"
            else -> "Transizione sconosciuta: $transitionType ($ids)"
        }

        //DEBUG:
        //Log.d("GEOFENCE_RECEIVER", message)
        //Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        showNotification(context, message)
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "geofence_channel"
        val channelName = "Geofence Alerts"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Luogo segnalato nei paraggi!!!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}


package com.example.myproject

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.myproject.ui.TripsStorical.TripRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

//Questo è il componente che ascolta gli eventi di geofencing del sistema
class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d("GeofenceReceiver", "Evento Geofence: partito")
        Toast.makeText(context, "Cambio poszione geofence", Toast.LENGTH_SHORT).show()
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return
        val transition = geofencingEvent.geofenceTransition
        /*DEBUG
        val message = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Sei ENTRATO nella zona"
            Geofence.GEOFENCE_TRANSITION_EXIT -> " Sei USCITO dalla zona"
            else -> " Transizione sconosciuta"
        }
        Toast.makeText(context?.applicationContext, message, Toast.LENGTH_LONG).show()
        */
    }
}
package com.example.myproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

//Questo è il componente che ascolta gli eventi di geofencing del sistema
class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {

        Log.d("GEOFENCING", "Received something")
        // nell' intent sono presenti idati le info del geofence
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent?.let {
                GeofenceStatusCodes
                    .getStatusCodeString(it.errorCode)
            } ?: "No geofencing event found!"
            Log.e("GEOFENCE", errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        var messageToDisplay = "Something weird happened with the transition types"

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // DEBUG
            messageToDisplay = triggeringGeofences.toString()
        }
        //DEBUG
        Toast.makeText(context, messageToDisplay, Toast.LENGTH_LONG).show()
    }
}
package com.example.myproject.Receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myProject.R
import com.example.myproject.MainActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class TransictionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) return

        val result = ActivityTransitionResult.extractResult(intent) ?: return

        // Itera su tutti gli eventi di transizione ricevuti determinandone il tipo
        result.transitionEvents.forEach { event ->
            val activity = when (event.activityType) {
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.RUNNING -> "RUNNING"
                DetectedActivity.ON_BICYCLE -> "BICYCLE"
                else -> "VEHICLE"
            }

            if (activity == null) {
                // Non fare nulla per UNKNOWN
                return@forEach
            }

            // Determina se l'utente ha appena iniziato (ENTER) o terminato (EXIT) quell’attività
            val transition = if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                "ENTER"
            } else {
                "EXIT"
            }

            //DEBUG
            //Log.d("ACTIVITY_RECOGNITION", "Evento: $activity $transition")
            //Toast.makeText(context, "Transizione: $activity $transition", Toast.LENGTH_SHORT).show()

            //Mostra notifica SOLO quando l’attività viene avviata (ENTER)
            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                showNotification(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "activity_channel"
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitoraggio attività",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentTitle("Vai da qulche parte?!")
            .setContentText("Vuoi iniziare a registrare il tuo viaggio e monitora le tue attività in tempo reale")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(2001, notification)
    }
}
package com.example.myproject.Notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myProject.R
import com.example.myproject.Repositories.TripRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InactivityWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("WORKER", "Worker avviato")

        val repository = TripRepository(applicationContext.applicationContext as Application)
        val lastTrip = repository.getMostRecentEndedTrip()

        if (lastTrip == null) {
            Log.d("WORKER", "Nessun viaggio trovato, esco")
            return Result.success()
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastEndDate = try {
            sdf.parse(lastTrip.endDate)
        } catch (e: Exception) {
            Log.e("WORKER", "Errore nel parsing data", e)
            return Result.success()
        }

        //Calcoliamo quanti giorni sono passati dall' ultimo viaggio registrato
        //nel database
        val daysElapsed = ((Date().time - lastEndDate.time) / (1000 * 60 * 60 * 24)).toInt()
        //Log.d("WORKER", "Giorni trascorsi: $daysElapsed")

        //DEBUG per inviare la notifica anche se c' è stato un viaggio a distanza di qualche minuto
        //if (daysElapsed >= 0) {
        //Log.d("WORKER", "Invio notifica")
        if (daysElapsed >= 30) {
            //Se la scadenza ha superato un mese allora mostriamo la notifica costruita
            //Passadno il messaggio nell' input
            showNotification("Ehi, è più di un mese che non fai un viaggio.")
        } else {
            Log.d("WORKER", "Viaggio troppo recente")
        }

        return Result.success()
    }


    private fun showNotification(message: String) {
        val channelId = "trip_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avvisi Viaggi",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Travel Companion")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)

        //Log.d("WORKER", "Notifica costruita, invio...")
        notificationManager.notify(1001, builder.build())
    }
}
package com.example.myproject.Notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myProject.R
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.TripPlanRepository
import com.example.myproject.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgramTripWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("DAILY_WORKER", "Worker avviato - controllo viaggi pianificati per oggi")

        val repository = TripPlanRepository(applicationContext as Application)

        // Recupera la data odierna nel formato usato dal database
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        // Recupera tutti i piani di viaggio aventi come data di partenza la data odierna
        val plans = repository.getAllPlansForDate(today)

        if (plans.isNotEmpty()) {
            plans.forEach { plan ->
                val typeLabel = when (plan.type) {
                    TripType.JOURNEY -> "una vacanza"
                    TripType.EXCURSION -> "una gita"
                    TripType.LOCAL -> "una passeggiata"
                }
                val msg = "Hai in programma di iniziare $typeLabel oggi verso ${plan.endPlace}."
                showNotification(msg)
            }
        } else {
            //DEBUG
            //Log.d("DAILY_WORKER", "Nessun viaggio pianificato per oggi")
        }

        return Result.success()
    }

    private fun showNotification(message: String) {
        val channelId = "trip_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avvisi Viaggi",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        //Intent per aprire l’app alla MainActivity quando si clicca la notifica
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Travel Companion")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
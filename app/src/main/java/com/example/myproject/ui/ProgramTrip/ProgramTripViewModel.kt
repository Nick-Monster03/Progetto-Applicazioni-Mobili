package com.example.myproject.ui.ProgramTrip

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myproject.Database.Entities.TripPlan
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.TripPlanRepository
import java.util.Calendar
import java.util.Locale

class ProgramTripViewModel(private val tripPlanRepository: TripPlanRepository) : ViewModel() {

    fun addTripPlan(plan: TripPlan): Long {
        return tripPlanRepository.insertTripPlan(plan)
    }

    //Verifica se esistono piani di viaggio già programmati che si sovrappongono alle date fornite
    suspend fun hasOverlappingTrip(start: String, end: String): Boolean {
        val plans = tripPlanRepository.getAllTripPlansRaw()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val newStart = dateFormat.parse(start)
        val newEnd = dateFormat.parse(end)
        return plans.any {
            val existingStart = dateFormat.parse(it.startDate)
            val existingEnd = dateFormat.parse(it.endDate)
            //La condizione restituisce true se c'è almeno una sovrapposizione tra due intervalli di date
            !(newEnd.before(existingStart) || newStart.after(existingEnd))
        }
    }

    fun validateTripPlanInput(
        tripTypeStr: String,
        startDate: String,
        endDate: String,
        startPlace: String?,
        endPlace: String
    ): String? {
        val type = when (tripTypeStr.uppercase()) {
            "JOURNEY" -> TripType.JOURNEY
            "EXCURSION" -> TripType.EXCURSION
            else -> TripType.LOCAL
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }

        //Parsing delle date, con controllo di formato
        val startDateParsed = try {
            dateFormat.parse(startDate)
        } catch (e: Exception) {
            return "Formato data di partenza non valido (usa yyyy-MM-dd)"
        }

        val endDateParsed = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            return "Formato data di arrivo non valido (usa yyyy-MM-dd)"
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Controlli generici validi per tutti i tipi di viaggio
        if (startDateParsed.before(today)) return "La data di partenza non può essere nel passato"
        if (endDateParsed.before(startDateParsed)) return "La data di arrivo non può essere precedente alla partenza"

        if (endPlace.isBlank()) return "Inserisci un luogo di destinazione"

        //Vincoli sui tipi di viaggio da rispettare
        if (type == TripType.LOCAL) {
            if (startPlace.isNullOrBlank()) return "Inserisci un luogo di partenza per un viaggio locale"
            if (startPlace.trim() != endPlace.trim()) return "Per un viaggio locale le città devono coincidere"
            if (startDate != endDate) return "Per un viaggio locale le date devono coincidere"
        }

        if (type == TripType.EXCURSION) {
            if (startDate != endDate) return "Per un'escursione le date devono coincidere"
        }

        return null //Nessun errore, allora input valido
    }

    class Factory(private val repo: TripPlanRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgramTripViewModel(repo) as T
        }
    }
}



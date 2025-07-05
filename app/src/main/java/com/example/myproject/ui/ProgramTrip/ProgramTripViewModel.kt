package com.example.myproject.ui.ProgramTrip

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myproject.Database.Entities.TripPlan
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.TripPlanRepository
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgramTripViewModel(private val tripPlanRepository: TripPlanRepository) : ViewModel() {

    fun addTripPlan(plan: TripPlan): Long {
        return tripPlanRepository.insertTripPlan(plan)
    }

    suspend fun hasOverlappingTrip(start: String, end: String): Boolean {
        val plans = tripPlanRepository.getAllTripPlansRaw()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val newStart = dateFormat.parse(start)
        val newEnd = dateFormat.parse(end)
        return plans.any {
            val existingStart = dateFormat.parse(it.startDate)
            val existingEnd = dateFormat.parse(it.endDate)
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

        if (startDateParsed.before(today)) return "La data di partenza non può essere nel passato"
        if (endDateParsed.before(startDateParsed)) return "La data di arrivo non può essere precedente alla partenza"

        if (endPlace.isBlank()) return "Inserisci un luogo di destinazione"

        // Vincoli specifici
        if (type == TripType.LOCAL) {
            if (startPlace.isNullOrBlank()) return "Inserisci un luogo di partenza per un viaggio locale"
            if (startPlace.trim() != endPlace.trim()) return "Per un viaggio locale le città devono coincidere"
            if (startDate != endDate) return "Per un viaggio locale le date devono coincidere"
        }

        if (type == TripType.EXCURSION) {
            if (startDate != endDate) return "Per un'escursione le date devono coincidere"
        }

        return null
    }

    class Factory(private val repo: TripPlanRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgramTripViewModel(repo) as T
        }
    }
}

/*
class ProgramTripViewModel(private val trip_repository: TripRepository, private val place_repository: PlaceRepository,
                           private val trip_place_repository: TripPlaceRepository
) : ViewModel(){


    fun addTrip(trip: Trip): Long {
        return trip_repository.insertTrip(trip)
    }

    fun addPlace(place: Place) {
        place_repository.insert(place)
    }

    fun addTripPlace(trip_place: TripPlace) {
        trip_place_repository.insertTripPlace(trip_place)
    }

    fun existPlace(place: Place): Boolean {
        return place_repository.existsPlace(place)
    }

    fun getPlace(place: Place): Int {
        return place_repository.getPlaceId(place)
    }

    fun validateTripInput(
        tripTypeStr: String,
        startDate: String,
        startPlace: String,
        endDate: String,
        endPlace: String,
        description: String
    ): String? {
        val type = when (tripTypeStr.uppercase()) {
            "JOURNEY" -> TripType.JOURNEY
            "EXCURSION" -> TripType.EXCURSION
            else -> TripType.LOCAL
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }

        fun parseDateOrNull(dateStr: String): Date? =
            try { dateFormat.parse(dateStr) } catch (e: Exception) { null }

        if (startDate.isBlank()) return "Inserisci una data di inizio"
        val startDateParsed = parseDateOrNull(startDate)
            ?: return "Formato data di inizio non valido (usa yyyy-MM-dd)"

        if (startPlace.isBlank()) return "Inserisci un luogo di partenza"

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (startDateParsed.before(today)) return "La data di inizio non può essere nel passato"

        if (type == TripType.EXCURSION || type == TripType.JOURNEY) {
            if (endPlace.isBlank() && endDate.isBlank()) {
                return "Inserisci almeno una data di fine o un luogo di arrivo"
            }
        }

        if (type == TripType.JOURNEY && endDate.isNotBlank()) {
            val endDateParsed = parseDateOrNull(endDate)
                ?: return "Formato data di fine non valido (usa yyyy-MM-dd)"

            if (endDateParsed.before(startDateParsed)) {
                return "La data di fine non può essere precedente alla data di inizio"
            }
            if (endDateParsed.before(today)) {
                return "La data di fine non può essere nel passato"
            }
        }

        return null
    }




    class ProgramTripViewModelFactory(
        private val trip_repository: TripRepository,
        private val place_repository: PlaceRepository,
        private val trip_place_repository: TripPlaceRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgramTripViewModel::class.java)) {
                return ProgramTripViewModel(trip_repository, place_repository, trip_place_repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
*/

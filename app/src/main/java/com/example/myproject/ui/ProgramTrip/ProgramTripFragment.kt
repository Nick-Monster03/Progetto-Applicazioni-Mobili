package com.example.myproject.ui.ProgramTrip

import android.app.AlertDialog
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myProject.R
import com.example.myproject.Database.Dao.PlaceDao
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Database.Entities.TripPlace
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.TripPlaceRepository
import com.example.myproject.repository.PlaceRepository
import com.example.myproject.ui.TripsStorical.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgramTripFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav_program_trip, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo_trip = TripRepository(requireActivity().application)
        val repo_place = PlaceRepository(requireActivity().application)
        val repo_pt = TripPlaceRepository(requireActivity().application)
        val factory = ProgramTripViewModel.ProgramTripViewModelFactory(repo_trip, repo_place, repo_pt)
        val programTripViewModel = ViewModelProvider(this, factory)[ProgramTripViewModel::class.java]

        val spinner = view.findViewById<Spinner>(R.id.spinnerTripType)
        val tripTypes = listOf("LOCAL", "EXCURSION", "JOURNEY")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tripTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val edit_start_date = view.findViewById<EditText>(R.id.editStartDate)
        val edit_start_place = view.findViewById<EditText>(R.id.editStartPlace)
        val edit_end_date = view.findViewById<EditText>(R.id.editEndDate)
        val edit_end_place = view.findViewById<EditText>(R.id.editDestination)
        val edit_description = view.findViewById<EditText>(R.id.editDescription)
        val btn_send = view.findViewById<Button>(R.id.buttonSaveTrip)

        if(spinner.selectedItem.toString() == "LOCAL") {
            edit_end_date.text = edit_start_date.text
            edit_end_date.isFocusable = false
            edit_end_date.isClickable = false
            edit_end_place.text = edit_start_place.text
            edit_end_place.isFocusable = false
            edit_end_place.isClickable = false
        }else if(spinner.selectedItem.toString() == "EXCURSION"){
                edit_end_date.text = edit_start_date.text
                edit_end_date.isFocusable = false
                edit_end_date.isClickable = false
                edit_end_place.setText("")
                edit_end_place.isFocusable = true
                edit_end_place.isClickable = true
        }else{
            edit_end_date.setText("")
            edit_end_date.isFocusable = true
            edit_end_date.isClickable = true
            edit_end_place.setText("")
            edit_end_place.isFocusable = true
            edit_end_place.isClickable = true
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()

                if (selected == "LOCAL") {
                    edit_end_date.setText(edit_start_date.text.toString())
                    edit_end_date.isFocusable = false
                    edit_end_date.isClickable = false

                    edit_end_place.setText(edit_start_place.text.toString())
                    edit_end_place.isFocusable = false
                    edit_end_place.isClickable = false
                } else if(selected == "EXCURSION"){
                    edit_end_place.setText("")
                    edit_end_place.isFocusableInTouchMode = true
                    edit_end_place.isClickable = true
                    edit_end_date.setText(edit_start_date.text.toString())
                    edit_end_date.isFocusable = false
                    edit_end_date.isClickable = false
                }
                else {
                    edit_end_date.setText("")
                    edit_end_date.isFocusableInTouchMode = true
                    edit_end_date.isClickable = true

                    edit_end_place.setText("")
                    edit_end_place.isFocusableInTouchMode = true
                    edit_end_place.isClickable = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        /*btn_send.setOnClickListener {
            var tripType = spinner.selectedItem.toString()
            var startDate = edit_start_date.text.toString()
            var startPlace = edit_start_place.text.toString().trim()
            var endDate = edit_end_date.text.toString()
            var endPlace = edit_end_place.text.toString().trim()
            var description = edit_description.text.toString()


            val valid = checkVariables(tripType, startDate, startPlace, endDate, endPlace, description) {
                    msg -> showError(msg)
            }
            if (!valid) return@setOnClickListener

            var type: TripType
            if(tripType == "JOURNEY"){
                type = TripType.JOURNEY
            }else if(tripType == "EXCURSION"){
                type = TripType.EXCURSION
            }else{
                type = TripType.LOCAL
            }
            if (type == TripType.LOCAL || type == TripType.EXCURSION)
                endDate = startDate

            if(type == TripType.LOCAL)
                endPlace = startPlace

            val trip = Trip(
                type = type,
                startDate = startDate,
                start = startPlace,
                endDate = endDate,
                destination = endPlace,
                description = description)



            Thread {
                val geocoder = Geocoder(requireActivity())
                val results = geocoder.getFromLocationName(startPlace, 1)
                val resultd = geocoder.getFromLocationName(endPlace, 1)

                if (!results.isNullOrEmpty() && !resultd.isNullOrEmpty()) {
                    val rawLatS = results[0].latitude
                    val rawLonS = results[0].longitude
                    val rawLatD = resultd[0].latitude
                    val rawLonD = resultd[0].longitude

                    //val precision = if (type == TripType.LOCAL) 4 else if (type == TripType.EXCURSION) 2 else 1
                    val precision = 4
                    val latS = String.format(Locale.US,"%.${precision}f", rawLatS).toDouble()
                    val lonS = String.format(Locale.US,"%.${precision}f", rawLonS).toDouble()
                    val latD = String.format(Locale.US,"%.${precision}f", rawLatD).toDouble()
                    val lonD = String.format(Locale.US,"%.${precision}f", rawLonD).toDouble()

                    // DEBUG
                    //Log.d("GEO", "$startPlace: $latS, $lonS")
                    //Log.d("GEO", "$endPlace: $latD, $lonD")

                    //Log.d("TRIP", "f{trip.start}: ${trip.start}, f{trip.destination}: ${trip.destination}, f{trip.startDate}: ${trip.startDate}, f{trip.endDate}: ${trip.endDate}, f{trip.description}: ${trip.description}, f{trip.type}: ${trip.type}")
                    val place_start = Place(id=0, latitudine = latS, longitudine = lonS, name = startPlace)
                    //Log.d("PLACE", "Place Start: id=${place_start.id}, latitudine=${place_start.latitudine}, longitudine=${place_start.longitudine}, name=${place_start.name}")
                    val place_destination = Place(id=0, latitudine = latD, longitudine = lonD, name = endPlace)
                    //Log.d("PLACE", "Place Destination: id=${place_destination.id}, latitudine=${place_destination.latitudine}, longitudine=${place_destination.longitudine}, name=${place_destination.name}")

                    if(programTripViewModel.existPlace(place_start)){
                        //Log.d("PLACE", "Place Start already exists in the database")
                    }else{
                        //Log.d("PLACE", "Place Start does not exist in the database, inserting...")
                        programTripViewModel.addPlace(place_start)
                    }
                    var id_place_start = programTripViewModel.getPlace(place_start)
                    if(programTripViewModel.existPlace(place_destination)){
                        //Log.d("PLACE", "Place Destination already exists in the database")
                    }else{
                        //Log.d("PLACE", "Place Destination does not exist in the database, inserting...")
                        programTripViewModel.addPlace(place_destination)
                    }
                    var id_place_destination = programTripViewModel.getPlace(place_destination)
                    //Log.d("PLACE", "id_place_start: $id_place_start, id_place_destination: $id_place_destination")
                    var id_trip = programTripViewModel.addTrip(trip)
                    //Log.d("TRIP_ID", "$id_trip: Trip added to the database with id $id_trip")
                    programTripViewModel.addTripPlace(TripPlace(id_trip.toInt(), id_place_start, time_stamp = java.time.LocalDate.now().toString()))
                    programTripViewModel.addTripPlace(TripPlace(id_trip.toInt(), id_place_destination,time_stamp = java.time.LocalDate.now().toString()))
                }
            }.start()
        }*/
        btn_send.setOnClickListener {
            //Recupero i dati dal form
            val tripType = spinner.selectedItem.toString()
            var startDate = edit_start_date.text.toString()
            var startPlace = edit_start_place.text.toString().trim()
            var endDate = edit_end_date.text.toString()
            var endPlace = edit_end_place.text.toString().trim()
            val description = edit_description.text.toString()
            //Chiamo il metodo di validazione sui vari dati e nel caso non dovvessero essere
            //corretti sarà mostrato un messaggiop di errore in base alla casistica
            val valid = checkVariables(tripType, startDate, startPlace, endDate, endPlace, description) {
                    msg -> showError(msg)
            }
            if (!valid) return@setOnClickListener

            val type = when (tripType) {
                "JOURNEY" -> TripType.JOURNEY
                "EXCURSION" -> TripType.EXCURSION
                else -> TripType.LOCAL
            }
            if (type == TripType.LOCAL || type == TripType.EXCURSION)
                endDate = startDate
            if (type == TripType.LOCAL)
                endPlace = startPlace

            val trip = Trip(
                type = type,
                startDate = startDate,
                start = startPlace,
                endDate = endDate,
                destination = endPlace,
                description = description
            )
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext())
                    val results = geocoder.getFromLocationName(startPlace, 1)
                    val resultd = geocoder.getFromLocationName(endPlace, 1)

                    // Gestione errore geocoder (magari siamo offline)
                    if (results.isNullOrEmpty() || resultd.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            showError("Impossibile trovare le coordinate dei luoghi. Verifica connessione e nomi.")
                        }
                        return@launch
                    }
                    val rawLatS = results[0].latitude
                    val rawLonS = results[0].longitude
                    val rawLatD = resultd[0].latitude
                    val rawLonD = resultd[0].longitude

                    val precision = 4
                    val latS = String.format(Locale.US, "%.${precision}f", rawLatS).toDouble()
                    val lonS = String.format(Locale.US, "%.${precision}f", rawLonS).toDouble()
                    val latD = String.format(Locale.US, "%.${precision}f", rawLatD).toDouble()
                    val lonD = String.format(Locale.US, "%.${precision}f", rawLonD).toDouble()

                    val place_start = Place(0, latS, lonS, startPlace)
                    val place_destination = Place(0, latD, lonD, endPlace)

                    if (!programTripViewModel.existPlace(place_start)) {
                        programTripViewModel.addPlace(place_start)
                    }
                    val id_place_start = programTripViewModel.getPlace(place_start)

                    if (!programTripViewModel.existPlace(place_destination)) {
                        programTripViewModel.addPlace(place_destination)
                    }
                    val id_place_destination = programTripViewModel.getPlace(place_destination)

                    val id_trip = programTripViewModel.addTrip(trip)
                    programTripViewModel.addTripPlace(
                        TripPlace(id_trip.toInt(), id_place_start, java.time.LocalDate.now().toString())
                    )
                    programTripViewModel.addTripPlace(
                        TripPlace(id_trip.toInt(), id_place_destination, java.time.LocalDate.now().toString())
                    )

                    // Conferma di successo
                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Successo")
                            .setMessage("Viaggio creato correttamente!")
                            .setPositiveButton("OK", null)
                            .show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showError("Errore durante la creazione del viaggio.")
                    }
                }
            }
        }

    }

    fun showError(message: String){
        AlertDialog.Builder(context)
            .setTitle("Errore")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()

    }

    fun checkVariables(
        tripTypeStr: String,
        startDate: String,
        startPlace: String,
        endDate: String,
        endPlace: String,
        description: String,
        showError: (String) -> Unit
    ): Boolean {
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

        if (startDate.isBlank()) {
            showError("Inserisci una data di inizio")
            return false
        }

        val startDateParsed = parseDateOrNull(startDate)
        if (startDateParsed == null) {
            showError("Formato data di inizio non valido (usa yyyy-MM-dd)")
            return false
        }

        if (startPlace.isBlank()) {
            showError("Inserisci un luogo di partenza")
            return false
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (startDateParsed.before(today)) {
            showError("La data di inizio non può essere nel passato")
            return false
        }

        if (type == TripType.EXCURSION || type == TripType.JOURNEY) {
            if (endPlace.isBlank()) {
                showError("Inserisci un luogo di arrivo")
                return false
            }
        }

        if (type == TripType.JOURNEY) {
            if (endDate.isBlank()) {
                showError("Inserisci una data di fine per il viaggio")
                return false
            }

            val endDateParsed = parseDateOrNull(endDate)
            if (endDateParsed == null) {
                showError("Formato data di fine non valido (usa yyyy-MM-dd)")
                return false
            }

            if (endDateParsed.before(startDateParsed)) {
                showError("La data di fine non può essere precedente alla data di inizio")
                return false
            }

            if (endDateParsed.before(today)) {
                showError("La data di fine non può essere nel passato")
                return false
            }
        }

        return true
    }

}
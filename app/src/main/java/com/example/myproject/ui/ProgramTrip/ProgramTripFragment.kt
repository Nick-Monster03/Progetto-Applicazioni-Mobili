package com.example.myproject.ui.ProgramTrip

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myProject.R
import com.example.myproject.Database.Entities.TripPlan
import com.example.myproject.Database.Entities.TripType
import com.example.myproject.Repositories.TripPlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgramTripFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repoPlan = TripPlanRepository(requireActivity().application)
        val factory = ProgramTripViewModel.Factory(repoPlan)
        val viewModel = ViewModelProvider(this, factory)[ProgramTripViewModel::class.java]

        val spinner = view.findViewById<Spinner>(R.id.spinnerTripType)
        val tripTypes = listOf("LOCAL", "EXCURSION", "JOURNEY")
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tripTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val editStartDate = view.findViewById<EditText>(R.id.editStartDate)
        val editStartPlace = view.findViewById<EditText>(R.id.editStartPlace)
        val editEndDate = view.findViewById<EditText>(R.id.editEndDate)
        val editEndPlace = view.findViewById<EditText>(R.id.editDestination)
        val editDescription = view.findViewById<EditText>(R.id.editDescription)
        val btnSave = view.findViewById<Button>(R.id.buttonSaveTrip)

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                //recupero variabili
                val tripType = spinner.selectedItem.toString()
                val startDate = editStartDate.text.toString()
                val endDate = editEndDate.text.toString()
                val startPlace = editStartPlace.text.toString().trim().ifBlank { null }
                val endPlace = editEndPlace.text.toString().trim()
                val description = editDescription.text.toString()

                val validationError = viewModel.validateTripPlanInput(
                    tripType,
                    startDate,
                    endDate,
                    startPlace,
                    endPlace
                )

                if (validationError != null) {
                    showError(validationError)
                    return@launch
                }

                // Controllo overlapping su Dispatcher.IO
                val hasOverlap = withContext(Dispatchers.IO) {
                    viewModel.hasOverlappingTrip(startDate, endDate)
                }

                if (hasOverlap) {
                    showError("Hai già un viaggio in programma per quel periodo.")
                    return@launch
                }

                val type = when (tripType) {
                    "JOURNEY" -> TripType.JOURNEY
                    "EXCURSION" -> TripType.EXCURSION
                    else -> TripType.LOCAL
                }

                val plan = TripPlan(
                    startDate = startDate,
                    endDate = endDate,
                    startPlace = startPlace,
                    endPlace = endPlace,
                    description = description,
                    type = type
                )

                withContext(Dispatchers.IO) {
                    viewModel.addTripPlan(plan)
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Piano creato")
                    .setMessage("Viaggio porgrammato con successo!")
                    .setPositiveButton("Ok") { _, _ -> }
                    .show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.nav_program_trip, container, false)

    private fun showError(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Errore")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}


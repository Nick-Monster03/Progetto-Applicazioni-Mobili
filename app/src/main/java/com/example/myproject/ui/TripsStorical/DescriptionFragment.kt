package com.example.myproject

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.ui.TripsStorical.TripAdapter
import com.example.myproject.Repositories.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel
import java.time.LocalDate

class DescriptionFragment : Fragment() {

    private lateinit var tripViewModel: TripViewModel
    private lateinit var tripAdapter: TripAdapter
    private lateinit var btn_filter: Button
    private lateinit var spinnerTipo: Spinner
    private lateinit var editDataStart: EditText
    private lateinit var editDataDestination: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.nav_storical, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerTipo = view.findViewById(R.id.spinner_trip_type)
        val tipiViaggio = listOf("ALL", "LOCAL", "EXCURSION", "JOURNEY")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("ALL", "LOCAL", "EXCURSION", "JOURNEY"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo = view.findViewById(R.id.spinner_trip_type)
        spinnerTipo.adapter = adapter


        editDataStart = view.findViewById(R.id.edittext_start_date_filter)
        editDataDestination = view.findViewById(R.id.edittext_destination_date_filter)
        btn_filter = view.findViewById(R.id.btn_filter)
        btn_filter.setOnClickListener(View.OnClickListener {
            filtraViaggi()
        })
        //Crea ViewModel con factory
        val repository = TripRepository(requireActivity().application)
        val factory = TripViewModel.TripViewModelFactory(repository)
        tripViewModel = ViewModelProvider(this, factory)[TripViewModel::class.java]

        //Inizializza l'adapter
        tripAdapter = TripAdapter(tripViewModel)

        //Imposta RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_trips)
        recyclerView.adapter = tripAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //Osserva LiveData
        tripViewModel.getAllTrips().observe(viewLifecycleOwner) { trips ->
            tripAdapter.submitList(trips)
        }
    }

    //Applico il filtro in base a tipo e intervallo di date
    @RequiresApi(Build.VERSION_CODES.O)
    fun filtraViaggi() {
        val tipo = spinnerTipo.selectedItem.toString()
        val start_date = editDataStart.text.toString()
        val destination_date = if (editDataDestination.text.toString().isNullOrEmpty()) LocalDate.now().toString() else editDataDestination.text.toString()

        //Controllo formato date
        if (!isValidDateFormat(start_date) || !isValidDateFormat(destination_date)) {
            showAlert("Formato date non corretto")
            return
        }

        //Controllo incongruenza tra date
        if (LocalDate.parse(start_date).isAfter(LocalDate.parse(destination_date))) {
            showAlert("La data di partenza non può essere posteriore a quella di destinazione")
            return
        }

        //Operazioni se i controlli sono superati
        tripViewModel.filtraViaggi(tipo, start_date, destination_date)
            .observe(viewLifecycleOwner) { trips ->
                tripAdapter.submitList(trips)
            }
    }

    //Funzione per verificare il formato delle date se rispetta il parsing
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isValidDateFormat(date: String): Boolean {
        return try {
            LocalDate.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    //Funzione per mostrare un AlertDialog in caso di errore
    private fun showAlert(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Errore")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

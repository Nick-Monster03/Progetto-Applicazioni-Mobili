package com.example.myproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.TravelDatabase
import com.example.myproject.ui.TripsStorical.TripAdapter
import com.example.myproject.ui.TripsStorical.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel

class DescriptionFragment : Fragment() {

    private lateinit var tripViewModel: TripViewModel
    private lateinit var tripAdapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.nav_add_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Crea ViewModel con factory
        val repository = TripRepository(requireActivity().application)
        val factory = TripViewModel.TripViewModelFactory(repository)
        tripViewModel = ViewModelProvider(this, factory)[TripViewModel::class.java]

        // Inizializza l'adapter
        tripAdapter = TripAdapter(tripViewModel)

        // Imposta RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_trips)
        recyclerView.adapter = tripAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Osserva LiveData
        tripViewModel.getAllTrips().observe(viewLifecycleOwner) { trips ->
            tripAdapter.submitList(trips)
        }
    }
}

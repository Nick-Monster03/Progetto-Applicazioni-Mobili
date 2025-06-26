package com.example.myproject.ui.Photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.ui.TripsStorical.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel

class AddPhotoFragment : Fragment() {

    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var tripViewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav_add_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripRepository = TripRepository(requireActivity().application)
        val factory = TripViewModel.TripViewModelFactory(tripRepository)
        tripViewModel = ViewModelProvider(this, factory)[TripViewModel::class.java]

        photoViewModel = ViewModelProvider(this)[PhotoViewModel::class.java]

        val outerRecycler = view.findViewById<RecyclerView>(R.id.outer_recycler)
        outerRecycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = GroupedPhotoAdapter(emptyList(), photoViewModel, viewLifecycleOwner, viewLifecycleOwner.lifecycleScope)
        outerRecycler.adapter = adapter

        tripViewModel.getAllTrips().observe(viewLifecycleOwner) { trips ->
            adapter.updateTrips(trips)
        }
    }

}
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch


class AddPhotoFragment : Fragment() {

    private lateinit var photoViewModel: PhotoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav_add_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoViewModel = ViewModelProvider(this)[PhotoViewModel::class.java]

        //Configuro il RecyclerView esterno, cioè quello con i gruppi di foto divisi in viaggi
        val outerRecycler = view.findViewById<RecyclerView>(R.id.outer_recycler)
        outerRecycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = GroupedPhotoAdapter(emptyList(), photoViewModel, viewLifecycleOwner, viewLifecycleOwner.lifecycleScope)
        outerRecycler.adapter = adapter

        //Osservo tutti i viaggi e filtro solo quelli che hanno almeno una foto
        photoViewModel.getAllTrips().observe(viewLifecycleOwner) { trips ->
            val coroutineScope = viewLifecycleOwner.lifecycleScope
            coroutineScope.launch {
                val tripsWithPhotos = trips.filter { trip ->
                    photoViewModel.getCountPhotosByTrip(trip.id) > 0
                }
                withContext(Dispatchers.Main) {
                    adapter.updateTrips(tripsWithPhotos)
                }
            }
        }
    }

}
package com.example.myproject.ui.Photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class AddPhotoFragment : Fragment() {

    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var placeViewModel: PlaceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav_add_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeViewModel = ViewModelProvider(this)[PlaceViewModel::class.java]
        val photoViewModel = ViewModelProvider(this)[PhotoViewModel::class.java]

        val outerRecycler = view.findViewById<RecyclerView>(R.id.outer_recycler)
        outerRecycler.layoutManager = LinearLayoutManager(requireContext())

        placeViewModel.getAllPlaces().observe(viewLifecycleOwner) { places ->
            val adapter = GroupedPhotoAdapter(
                places,
                photoViewModel,
                viewLifecycleOwner
            )
            outerRecycler.adapter = adapter
        }
    }

}
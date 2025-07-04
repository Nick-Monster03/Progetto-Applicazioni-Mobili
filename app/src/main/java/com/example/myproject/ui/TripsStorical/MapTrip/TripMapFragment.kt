package com.example.myproject.ui.TripsStorical.MapTrip

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myProject.R
import com.example.myproject.Repositories.TripRepository
import com.example.myproject.ui.TripsStorical.TripViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

class TripMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var tripViewModel: TripViewModel
    private lateinit var googleMap: GoogleMap
    private var tripId: Int = -1
    private var polyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripId = requireArguments().getInt("trip_id", -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_trip_layout, container, false)

        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.GONE

        val factory =
            TripViewModel.TripViewModelFactory(TripRepository(requireActivity().application))
        tripViewModel = ViewModelProvider(this, factory)[TripViewModel::class.java]

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_trip_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_tripMapFragment_to_descriptionFragment)
        }

        return view
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        tripViewModel.getPlacesById(tripId).observe(viewLifecycleOwner) { places ->
            if (places.isNotEmpty()) {
                val polylineOptions = PolylineOptions().color(Color.RED).width(6f)
                places.forEach {
                    polylineOptions.add(LatLng(it.latitudine, it.longitudine))
                }
                googleMap.addPolyline(polylineOptions)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(places[0].latitudine, places[0].longitudine), 15f))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
    }
}


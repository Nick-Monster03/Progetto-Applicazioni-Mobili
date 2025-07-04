package com.example.myproject.ui.Statistics


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myProject.R
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.Repositories.TripRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import java.time.YearMonth

class StatisticsFragment : Fragment(R.layout.nav_statistics), OnMapReadyCallback {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var chart: BarChart
    private lateinit var map: GoogleMap
    private lateinit var lineChart: LineChart


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Solo se necessario, evita di tagliare la UI


        // Inizializza il repository
        val repository = TripRepository(requireActivity().application)
        val factory = StatisticsViewModel.StatisticsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StatisticsViewModel::class.java]

        chart = view.findViewById(R.id.bar_chart)
        lineChart = view.findViewById(R.id.line_chart)


        val spinner = view.findViewById<Spinner>(R.id.spinner_filter)
        val button = view.findViewById<Button>(R.id.button_apply_filter)

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            PeriodFilter.values()
        )

        button.setOnClickListener {
            val selected = spinner.selectedItem as PeriodFilter
            viewModel.setFilter(selected)
        }

        val existingMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment)
        val mapFragment = existingMapFragment as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.map_fragment, it)
                    .commitNow()
            }
        mapFragment.getMapAsync(this)


        // Osserva i viaggi filtrati
        viewModel.filteredTrips.observe(viewLifecycleOwner) { trips ->
            updateChart(trips)
            viewModel.computeHeatmapPoints(trips)
            updateLineChart(trips)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateChart(trips: List<Trip>) {
        val monthLabels = mutableListOf<String>()
        val entries = mutableListOf<BarEntry>()

        val grouped = trips.groupBy {
            YearMonth.parse(it.startDate.take(7)) // es. "2024-03"
        }.toSortedMap() // Ordina cronologicamente

        var index = 0f
        for ((month, tripsInMonth) in grouped) {
            val label = "${month.monthValue.toString().padStart(2, '0')}/${month.year % 100}"
            monthLabels.add(label)
            entries.add(BarEntry(index, tripsInMonth.size.toFloat()))
            index += 1f
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.apply {
            granularity = 1f
            setDrawLabels(true)
            valueFormatter = IndexAxisValueFormatter(monthLabels)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }
        chart.axisLeft.apply {
            granularity = 1f
        }

        val dataSet = BarDataSet(entries, "Numero viaggi")
        dataSet.color = Color.BLUE

        chart.data = BarData(dataSet)
        chart.invalidate()
    }
/*
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMap(tripIds: List<Int>) {
        map.clear()
        tripIds.forEach { tripId ->
            viewModel.getPlacesForTrip(tripId).observe(viewLifecycleOwner) { places ->
                val points = places.map { LatLng(it.latitudine, it.longitudine) }
                if (points.size > 1) {
                    val polyline = PolylineOptions().addAll(points).color(Color.RED)
                    map.addPolyline(polyline)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last(), 8f))
                }
            }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChart(trips: List<Trip>) {
        val sortedTrips = trips.sortedBy { it.startDate }

        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        var collected = 0
        val total = sortedTrips.size

        if (total == 0) {
            lineChart.clear()
            return
        }

        sortedTrips.forEachIndexed { index, trip ->
            viewModel.getPlacesForTrip(trip.id).observe(viewLifecycleOwner) { places ->
                val distance = viewModel.calculateTotalDistance(places).toFloat()

                entries.add(Entry(index.toFloat(), distance))
                labels.add("Trip ${index + 1}")

                collected++
                if (collected == total) {
                    entries.sortBy { it.x }
                    val dataSet = LineDataSet(entries, "Distanza per viaggio").apply {
                        color = Color.GREEN
                        setCircleColor(Color.BLACK)
                        setDrawCircles(true)
                        setDrawValues(true)
                        lineWidth = 2f
                    }

                    lineChart.apply {
                        data = LineData(dataSet)
                        axisRight.isEnabled = false
                        description.isEnabled = false
                        legend.isEnabled = false
                        xAxis.apply {
                            granularity = 1f
                            setDrawLabels(true)
                            valueFormatter = IndexAxisValueFormatter(labels)
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        }
                        //Forziamo il ridisegnamento automatico
                        invalidate()
                    }
                }
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        viewModel.filteredTrips.observe(viewLifecycleOwner) { trips ->
            viewModel.computeHeatmapPoints(trips)
        }

        viewModel.heatmapPoints.observe(viewLifecycleOwner) { points ->
            val heatmapProvider = HeatmapTileProvider.Builder()
                .data(points)
                .radius(20)
                .build()
            map.addTileOverlay(TileOverlayOptions().tileProvider(heatmapProvider))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last(), 12f))
        }


    }

}

package com.example.myproject.ui.TripsStorical

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textDates = view.findViewById<TextView>(R.id.text_trip_dates)
    val textRoute = view.findViewById<TextView>(R.id.text_trip_route)
    val textType = view.findViewById<TextView>(R.id.text_trip_type)
    val textDescription = view.findViewById<TextView>(R.id.text_trip_description)
    val textDistance = view.findViewById<TextView>(R.id.text_trip_distance)
    val buttonNotes = view.findViewById<Button>(R.id.btn_show_notes)
}


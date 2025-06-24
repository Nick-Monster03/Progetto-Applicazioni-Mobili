package com.example.myproject.ui.TripsStorical


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.myProject.R
import com.example.myproject.Database.Entities.Trip
import com.example.myproject.ui.TripsStorical.MapTrip.TripMapFragment

class TripAdapter (private val viewModel: TripViewModel) : Adapter<TripViewHolder>() {

    private var trips: List<Trip> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_card, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        if (trip != null) {
            holder.textDates.text = "${trip.startDate} / ${trip.endDate}"
            holder.textRoute.text = "${trip.start} -> ${trip.destination}"
            holder.textType.text = trip.type.toString()
            holder.textDescription.text = trip.description ?: ""
            holder.buttonNotes.findViewById<Button>(R.id.btn_show_notes).setOnClickListener {
                //DEBUG
                //Toast.makeText(holder.itemView.context, "Mostra note", Toast.LENGTH_SHORT).show()
                val context = holder.itemView.context

                val navController = Navigation.findNavController(holder.itemView)
                val bundle = Bundle().apply {
                    putInt("trip_id", trip.id)
                }
                navController.navigate(R.id.action_descriptionFragment_to_nav_notes, bundle)

            }

            var lifecycleScope = (holder.itemView.context as AppCompatActivity).lifecycleScope
            val lifecycleOwner = holder.itemView.context as AppCompatActivity
            viewModel.getDistanceById(trip.id).observe(lifecycleOwner) { distanzaKm ->
                holder.textDistance.text = "Distanza: %.1f km".format(distanzaKm)
            }




            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val bundle = Bundle().apply {
                    putInt("trip_id", trip.id)
                }

                val fragment = TripMapFragment()
                fragment.arguments = bundle

                val navController = Navigation.findNavController(holder.itemView)
                navController.navigate(R.id.action_descriptionFragment_to_tripMapFragment, bundle)
            }

        }

    }
    fun submitList(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    //DEBUG
    override fun getItemCount(): Int {
        return trips.size?: 0
    }
}
package com.example.myproject.ui.Photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.Entities.Photo
import com.example.myproject.Database.Entities.Place
import com.example.myproject.Database.Entities.Trip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupedPhotoAdapter(
    private var trips: List<Trip>,
    private val photoViewModel: PhotoViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val coroutineScope: CoroutineScope,
) : RecyclerView.Adapter<GroupedViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grouped_photo, parent, false)
        return GroupedViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupedViewHolder, position: Int) {
        val trip = trips[position]
        val photoAdapter = PhotoAdapter()
        holder.recycler.layoutManager = LinearLayoutManager(holder.recycler.context, LinearLayoutManager.HORIZONTAL, false)
        holder.recycler.adapter = photoAdapter
        /*coroutineScope.launch {
            val count = photoViewModel.getCountPhotosByTrip(trip.id)
            withContext(Dispatchers.Main) {
                if (count == 0) {
                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                } else {
                    holder.itemView.visibility = View.VISIBLE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    holder.title.text = "${trip.start} - ${trip.destination}"
                }
            }
        }*/
        holder.title.text = "${trip.start} - ${trip.destination}"

        photoViewModel.getPhotosByTrip(trip.id).observe(lifecycleOwner) {
            photoAdapter.submitList(it)
        }
    }

    override fun getItemCount() = trips.size

    fun updateTrips(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }
}

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

class GroupedPhotoAdapter(
    private val places: List<Place>,
    private val photoViewModel: PhotoViewModel,
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<GroupedViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grouped_photo, parent, false)
        return GroupedViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupedViewHolder, position: Int) {
        val place = places[position]
        holder.title.text = place.name

        val photoAdapter = PhotoAdapter()
        holder.recycler.layoutManager = LinearLayoutManager(holder.recycler.context, LinearLayoutManager.HORIZONTAL, false)
        holder.recycler.adapter = photoAdapter

        photoViewModel.getPhotosForPlace(place.id).observe(lifecycleOwner) {
            photoAdapter.submitList(it)
        }
    }

    override fun getItemCount() = places.size
}

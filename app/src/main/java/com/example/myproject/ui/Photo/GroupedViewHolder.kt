package com.example.myproject.ui.Photo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class GroupedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById<TextView>(R.id.trip_title)
    val recycler = view.findViewById<RecyclerView>(R.id.photo_list)
}
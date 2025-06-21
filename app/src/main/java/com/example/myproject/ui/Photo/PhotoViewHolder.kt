package com.example.myproject.ui.Photo

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.photo_image)
    val timestamp: TextView = view.findViewById(R.id.photo_timestamp)
}
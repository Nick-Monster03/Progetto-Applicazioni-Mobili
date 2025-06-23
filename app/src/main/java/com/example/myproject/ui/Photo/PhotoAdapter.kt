package com.example.myproject.ui.Photo

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.Entities.Photo
import java.io.File

class PhotoAdapter : RecyclerView.Adapter<PhotoViewHolder>() {

    private var photos = listOf<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.photos_place_card, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        val file = File(photo.image_path)

        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(photo.image_path)
            if (bitmap != null) {
                holder.image.setImageBitmap(bitmap)
            } else {
                holder.image.setImageResource(R.drawable.place_holder)
            }
        } else {
            holder.image.setImageResource(R.drawable.place_holder)
        }

        holder.timestamp.text = photo.timestamp
    }

    override fun getItemCount() = photos.size

    fun submitList(list: List<Photo>) {
        photos = list
        notifyDataSetChanged()
    }
}

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

class PhotoAdapter : RecyclerView.Adapter<PhotoViewHolder>() {

    private var photos = listOf<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.photos_place_card, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        //Essendo memorizzate come Blob le immagini vanno deserializzate poi mandate
        //a schermo
        val bitmap = BitmapFactory.decodeByteArray(photo.photoBlob, 0, photo.photoBlob.size)
        holder.image.setImageBitmap(bitmap)
        holder.timestamp.text = photo.timestamp
    }

    override fun getItemCount() = photos.size

    fun submitList(list: List<Photo>) {
        photos = list
        notifyDataSetChanged()
    }
}

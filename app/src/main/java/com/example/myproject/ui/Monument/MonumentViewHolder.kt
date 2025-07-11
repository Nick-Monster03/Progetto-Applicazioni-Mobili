package com.example.myproject.ui.Monument

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class MonumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val nameText: TextView = view.findViewById(R.id.textMonumentName)
    val checkBox: CheckBox = view.findViewById(R.id.checkGeofence)
}


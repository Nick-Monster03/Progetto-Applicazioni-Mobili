package com.example.myproject.ui.TripsStorical.Note

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R

class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val txtNoteContent: TextView = view.findViewById(R.id.text_note_content)
    val txtNoteDate: TextView = view.findViewById(R.id.text_note_date)
}

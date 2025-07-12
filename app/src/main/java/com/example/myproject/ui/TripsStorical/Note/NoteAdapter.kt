package com.example.myproject.ui.TripsStorical.Note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.Entities.Note

class NoteAdapter : RecyclerView.Adapter<NoteViewHolder>() {

    private var notes: List<Note> = emptyList()//lista di note inizialmente settata a vuota

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        //per rappresentare una singola nota usiamo un layout chiamato note_card.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.txtNoteContent.text = note.content
        holder.txtNoteDate.text = note.timestamp.toString()
    }

    fun submitList(list: List<Note>) {
        notes = list
        notifyDataSetChanged()
    }
}

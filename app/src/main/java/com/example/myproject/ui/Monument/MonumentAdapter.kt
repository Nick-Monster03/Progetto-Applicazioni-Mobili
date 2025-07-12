package com.example.myproject.ui.Monument

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.Entities.Monument

class MonumentAdapter(private val onCheckChanged: (Monument) -> Unit) : RecyclerView.Adapter<MonumentViewHolder>() {

    private var monuments = listOf<Monument>()
    //Accesso pubblico alla lista corrente (usato in precedenza per il ViewModel, ma adesso inutilizzato)
    val currentList: List<Monument>
        get() = monuments

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_monument, parent, false)
        return MonumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonumentViewHolder, position: Int) {
        val monument = monuments[position]
        holder.nameText.text = monument.name

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = monument.isChecked
        holder.checkBox.setOnCheckedChangeListener { _, _ ->
            onCheckChanged(monument)//callback per gestire il cambio di stato della checkbox
        }
    }

    override fun getItemCount() = monuments.size

    fun submitList(list: List<Monument>) {
        monuments = if (list.isNotEmpty()) list else emptyList()
        notifyDataSetChanged()//dopo l' aggiornamento della lista opvviamento dobbiamo notificare il recyclerView
    }
}




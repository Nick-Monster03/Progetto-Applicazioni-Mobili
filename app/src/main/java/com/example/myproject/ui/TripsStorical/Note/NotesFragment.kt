package com.example.myproject.ui.TripsStorical.Note.NotesFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.ui.TripsStorical.Note.NoteAdapter
import com.example.myproject.ui.TripsStorical.Note.NoteViewModel

class NotesFragment : Fragment() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var btn_back: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        /*DEBUG
        Toast.makeText(requireContext(), "QUALCOSA", Toast.LENGTH_SHORT).show()
        Log.d("NotesFragment", "onCreateView chiamato")*/

        // Nasconde temporaneamente la toolbar per una visualizzazione più pulita delle note
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.GONE
        return inflater.inflate(R.layout.nav_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera l'id del viaggio passato tramite arguments
        val tripId = arguments?.getInt("trip_id") ?: -1
        //DEBUG
        // Toast.makeText(requireContext(), "Trip ID: $tripId", Toast.LENGTH_SHORT).show()
        if (tripId == -1) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        viewModel = ViewModelProvider(this, NoteViewModel.Factory(requireActivity().application, tripId))[NoteViewModel::class.java]

        // Inizializza l’adapter e configura il RecyclerView con layout verticale
        adapter = NoteAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_notes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Osserva il LiveData delle note: ogni cambiamento aggiorna la lista mostrata, anche se non serve dato che il viaggio
        //è già stato concluso. Inoltre il LiveData garantisce compatibilità futura e reattività
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            //DEBUG
            //Toast.makeText(requireContext(), "Note caricate: ${notes.size}", Toast.LENGTH_SHORT).show()
            adapter.submitList(notes)
        }

        //Imposto il pulsante(ImageButton) "indietro" per ripristinare la toolbar e tornare alla schermata precedente
        btn_back= view.findViewById<ImageButton>(R.id.btn_back)
        btn_back.setOnClickListener {
            requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
            requireActivity().onBackPressedDispatcher.onBackPressed()// Torna indietro alla schermata precedente senza usare action
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
    }
}


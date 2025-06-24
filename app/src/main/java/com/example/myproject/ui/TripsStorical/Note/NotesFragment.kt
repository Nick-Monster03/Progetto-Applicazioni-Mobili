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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*DEBUG
        Toast.makeText(requireContext(), "QUALCOSA", Toast.LENGTH_SHORT).show()
        Log.d("NotesFragment", "onCreateView chiamato")*/
        requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.GONE
        return inflater.inflate(R.layout.nav_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripId = arguments?.getInt("trip_id") ?: -1
        //DEBUG
        // Toast.makeText(requireContext(), "Trip ID: $tripId", Toast.LENGTH_SHORT).show()
        if (tripId == -1) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        viewModel = ViewModelProvider(this, NoteViewModel.Factory(requireActivity().application, tripId))[NoteViewModel::class.java]
        adapter = NoteAdapter()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_notes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            //DEBUG
            //Toast.makeText(requireContext(), "Note caricate: ${notes.size}", Toast.LENGTH_SHORT).show()
            adapter.submitList(notes)
        }



        btn_back= view.findViewById<ImageButton>(R.id.btn_back)
        btn_back.setOnClickListener {
            requireActivity().findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}


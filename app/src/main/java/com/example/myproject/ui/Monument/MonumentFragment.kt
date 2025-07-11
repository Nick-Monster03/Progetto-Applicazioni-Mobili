package com.example.myproject.ui.Monument

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myProject.R
import com.example.myproject.Database.Entities.Monument
import com.example.myproject.Receiver.MonumentGeofenceReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MonumentFragment : Fragment() {

    private lateinit var viewModel: MonumentViewModel
    private lateinit var adapter: MonumentAdapter
    private lateinit var geofencingClient: GeofencingClient
    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), MonumentGeofenceReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(requireContext(), 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.nav_monuments_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            MonumentViewModel.MonumentViewModelFactory(requireActivity().application)
        )[MonumentViewModel::class.java]

        ensureLocationPermissions()
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMonuments)
        val searchInput = view.findViewById<EditText>(R.id.editSearch)
        val addButton = view.findViewById<Button>(R.id.buttonAddMonument)
        val saveButton = view.findViewById<Button>(R.id.buttonSave)

        adapter = MonumentAdapter { monument ->
            viewModel.changeCheck(monument.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Osserva i dati filtrati
        viewModel.filteredMonuments.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        searchInput.addTextChangedListener {
            viewModel.setSearchQuery(it?.toString() ?: "")
        }

        // Aggiunta nuovo monumento
        addButton.setOnClickListener {
            showAddMonumentDialog()
        }

        // Salvataggio Geofence attivi
        saveButton.setOnClickListener {
            geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener {
                //DEBUG:
                // Log.d("GEOFENCE", "Geofence rimossi")
                val checkedMonuments = viewModel.getCheckedMonuments().value

                //DEBUG:
                // Log.d("GEOFENCE", "Aggiunti questi geofence: ${checkedMonuments.map { it.name }}")

                addGeofences(checkedMonuments ?: emptyList())//La lista potrebbe essere vuota, ma anche quella casistica è gestita nell' add geofences
            }

        }
    }

    private fun addGeofences(monuments: List<Monument>) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        if (monuments.isEmpty()) {
            geofencingClient.removeGeofences(pendingIntent)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Tutti i geofence rimossi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("GEOFENCE", "Errore nella rimozione dei geofence: ${it.message}")
                }
            return
        }

        val geofenceList = monuments.map { monument ->
            Geofence.Builder()
                .setRequestId(monument.name)
                .setCircularRegion(monument.lat, monument.lon, 200f)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }
        //DEBUG:
        // Log.d("GEOFENCE", "Aggiunti questi geofence: ${geofenceList.map { it.requestId }}")


        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Geofence attivati", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add geofece ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /*private fun removeGeofence(monument: Monument) {
        geofencingClient.removeGeofences(listOf(monument.id.toString()))
            .addOnSuccessListener { Log.d("GEOFENCE", "Removed for ${monument.name}") }
            .addOnFailureListener { Log.e("GEOFENCE", "Failed to remove for ${monument.name}") }
    }*/

    private fun ensureLocationPermissions() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val backgroundLocation = Manifest.permission.ACCESS_BACKGROUND_LOCATION

        val missing = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), fineLocation) != PackageManager.PERMISSION_GRANTED) {
            missing.add(fineLocation)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(requireContext(), backgroundLocation) != PackageManager.PERMISSION_GRANTED) {
            missing.add(backgroundLocation)
        }

        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 1001)
        }
    }




    private fun showAddMonumentDialog() {
        val input = EditText(requireContext())
        input.hint = "Es. Colosseo, Roma"

        AlertDialog.Builder(requireContext())
            .setTitle("Aggiungi Monumento")
            .setView(input)
            .setPositiveButton("Aggiungi") { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    lifecycleScope.launch {
                        val location = withContext(Dispatchers.IO) {
                            try {
                                geocoder.getFromLocationName(name, 1)?.firstOrNull()
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (location != null) {
                            val monument = Monument(
                                name = name,
                                lat = location.latitude,
                                lon = location.longitude
                            )
                            viewModel.insert(monument)
                        } else {
                            Toast.makeText(requireContext(), "Luogo non trovato", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }


}

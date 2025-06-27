package com.example.myproject.ui.map

import android.app.AlertDialog
import android.content.Context
import com.example.myproject.Database.Entities.TripType

fun Context.selectTripTypeDialog(onSelected: (TripType) -> Unit) {
    val types = TripType.values().map { it.name.replace("_", " ") }
    AlertDialog.Builder(this)
        .setTitle("Tipo di viaggio")
        .setItems(types.toTypedArray()) { _, index ->
            onSelected(TripType.values()[index])
        }
        .show()
}
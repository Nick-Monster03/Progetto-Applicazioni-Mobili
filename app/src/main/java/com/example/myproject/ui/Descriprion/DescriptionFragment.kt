package com.example.myproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myProject.R

class DescriptionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Puoi usare anche un layout minimale, tipo uno con solo un TextView
        return inflater.inflate(R.layout.nav_add_description, container, false)
    }
}

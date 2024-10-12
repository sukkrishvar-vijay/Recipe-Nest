package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Remove the default title
        toolbar.title = ""

        // Create and set a custom TextView for centered title
        val titleTextView = TextView(requireContext())
        titleTextView.text = "Favorites"
        titleTextView.textSize = 20f // Adjust the text size as needed
        titleTextView.setTextColor(resources.getColor(android.R.color.black, null))
        titleTextView.gravity = View.TEXT_ALIGNMENT_CENTER

        // Set the TextView layout parameters to center it in the Toolbar
        val layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = android.view.Gravity.CENTER
        toolbar.addView(titleTextView, layoutParams)

        return rootView
    }
}

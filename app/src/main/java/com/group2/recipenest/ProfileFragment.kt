package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title directly
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set click listener on "My Recipes" tile (list_item_title1)
        val myRecipesTile = rootView.findViewById<TextView>(R.id.list_item_title1)
        myRecipesTile.setOnClickListener {
            // Navigate to MyRecipesFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyRecipesFragment())
                .addToBackStack(null)
                .commit()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        // Reset the toolbar when FavoritesFragment is resumed
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Remove the navigation icon (back button)
        toolbar.navigationIcon = null  // This removes the back button from the toolbar
    }
}

package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Find the tile and set an OnClickListener
        val openRecipeCardsTile = rootView.findViewById<ConstraintLayout>(R.id.favorite_collection_tile)
        openRecipeCardsTile.setOnClickListener {
            // Navigate to RecipeCardsFragment using FragmentTransaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeCardsFragment())  // Replace with your fragment container ID
                .addToBackStack(null)  // Add this transaction to the back stack
                .commit()
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()

        // Reset the toolbar when FavoritesFragment is resumed
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Remove the navigation icon (back button)
        toolbar.navigationIcon = null  // This removes the back button from the toolbar
    }
}

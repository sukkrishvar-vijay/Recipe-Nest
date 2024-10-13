package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Set up RecyclerView
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // List of tiles with dynamic data
        val tileList = listOf(
            Tile("Breakfast", 0),
            Tile("Lunch", 0),
            Tile("Snack", 0),
            Tile("Dinner", 0)
        )

        // Set adapter with tile list
        val adapter = FavoritesTileAdapter(tileList) { tile ->
            // Handle tile click, navigate to RecipeCardsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipeCardsFragment())
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

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

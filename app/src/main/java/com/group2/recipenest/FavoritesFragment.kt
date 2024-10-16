package com.group2.recipenest

import FavoriteCollectionsTileModel
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
            FavoriteCollectionsTileModel("Breakfast", 0),
            FavoriteCollectionsTileModel("Lunch", 0),
            FavoriteCollectionsTileModel("Snack", 0),
            FavoriteCollectionsTileModel("Dinner", 0)
        )

        // Set adapter with tile list
        val adapter = FavoritesTileAdapter(tileList) { tile ->
            // Pass the tile title (e.g., "Breakfast") to RecipeCardsFragment
            val recipeCardsFragment = RecipeCardsFragment.newInstance(tile.title)
            // Handle tile click, navigate to RecipeCardsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, recipeCardsFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set up Floating Action Button (FAB) to show the dialog
        val fabAddCollection = rootView.findViewById<FloatingActionButton>(R.id.fab_add_collection)
        fabAddCollection.setOnClickListener {
            showAddCollectionDialog()
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

    // Function to show the Add Collection dialog
    private fun showAddCollectionDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_collection, null)

        // Create the AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        // Set up the buttons
        val dialog = builder.create()

        // Set the window background to transparent to ensure rounded corners show correctly
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss() // Close the dialog on Cancel
        }

        dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
            val collectionName = dialogView.findViewById<EditText>(R.id.input_collection_name).text.toString()
            // Handle the entered collection name here (e.g., save it or display a message)
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }
}

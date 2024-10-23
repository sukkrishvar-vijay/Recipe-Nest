package com.group2.recipenest

import FavoriteCollectionsTileModel
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FavoritesFragment : Fragment() {

    // Firestore instance
    private lateinit var firestore: FirebaseFirestore

    // User ID
    private val currentUserId = userSignInData.UserDocId

    private lateinit var adapter: FavoritesTileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Set up RecyclerView
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the adapter with an empty list initially
        adapter = FavoritesTileAdapter(emptyList()) { tile ->
            // Pass the tile title to RecipeCardsFragment
            val recipeCardsFragment = RecipeCardsFragment.newInstance(tile.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, recipeCardsFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        // Fetch the favorite collections when the page loads
        fetchFavoriteCollections()

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
        toolbar.navigationIcon = null

        // Refresh the favorite collections when the fragment is resumed
        fetchFavoriteCollections()
    }

    // Function to fetch favorite collections and update the counts
    private fun fetchFavoriteCollections() {
        // Fetch the User document from Firestore
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the favoriteCollection field
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                // Create a mutable list to hold updated tiles based on dynamic data
                val updatedTileList = mutableListOf<FavoriteCollectionsTileModel>()

                // Iterate over each map in the favoriteCollection
                favoriteCollection?.forEach { collection ->
                    // Each map contains one key-value pair
                    collection.forEach { (category, recipeIds) ->
                        // Add a new tile with the key as the title and the count as the array size
                        updatedTileList.add(FavoriteCollectionsTileModel(category.uppercase(), recipeIds.size))
                    }
                }

                // Update the adapter with the new tile list
                adapter.updateTiles(updatedTileList)
            }
        }.addOnFailureListener { exception ->
            // Handle failure case here
            exception.printStackTrace()
        }
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
            val collectionName = dialogView.findViewById<TextInputEditText>(R.id.input_collection_name).text.toString()

            if (collectionName.isNotEmpty()) {
                // Call the function to add the new collection to Firestore
                addNewCollection(collectionName.lowercase())
            }

            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }

    // Function to add a new collection to the favoriteCollection array field in Firestore
    private fun addNewCollection(newCollectionName: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        // Add a new map entry for the new collection
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                // Create a new map for the new collection
                val newCollection = mapOf(newCollectionName to listOf<String>())

                // Update the existing collection by adding the new collection map
                val updatedCollection = favoriteCollection?.toMutableList() ?: mutableListOf()
                updatedCollection.add(newCollection)

                // Update the favoriteCollection field in Firestore
                userRef.update("favoriteCollection", updatedCollection).addOnSuccessListener {
                    // After adding, refresh the page
                    fetchFavoriteCollections()
                }.addOnFailureListener { exception ->
                    // Handle error while updating the collection
                    exception.printStackTrace()
                }
            }
        }.addOnFailureListener { exception ->
            // Handle error in fetching user data
            exception.printStackTrace()
        }
    }
}

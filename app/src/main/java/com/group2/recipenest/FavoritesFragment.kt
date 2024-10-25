/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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

    private lateinit var firestore: FirebaseFirestore
    private val currentUserId = userSignInData.UserDocId
    private lateinit var adapter: FavoritesTileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        firestore = Firebase.firestore

        // RecyclerView setup with adapter adapted from Android documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = FavoritesTileAdapter(emptyList()) { tile ->
            val recipeCardsFragment = RecipeCardsFragment.newInstance(tile.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, recipeCardsFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        fetchFavoriteCollections()

        // Toolbar title setup and customization based on Android developer documentation
        // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // FloatingActionButton usage adapted from Android developer documentation
        // https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton
        val fabAddCollection = rootView.findViewById<FloatingActionButton>(R.id.fab_add_collection)
        fabAddCollection.setOnClickListener {
            showAddCollectionDialog()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.navigationIcon = null

        fetchFavoriteCollections()
    }

    // Firestore document retrieval adapted from Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    private fun fetchFavoriteCollections() {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                val updatedTileList = mutableListOf<FavoriteCollectionsTileModel>()

                favoriteCollection?.forEach { collection ->
                    collection.forEach { (category, recipeIds) ->
                        updatedTileList.add(FavoriteCollectionsTileModel(category.uppercase(), recipeIds.size))
                    }
                }

                adapter.updateTiles(updatedTileList)
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    // AlertDialog setup and display learned from Android documentation
    // https://developer.android.com/guide/topics/ui/dialogs
    private fun showAddCollectionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_collection, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
            val collectionName = dialogView.findViewById<TextInputEditText>(R.id.input_collection_name).text.toString()

            if (collectionName.isNotEmpty()) {
                addNewCollection(collectionName.lowercase())
            }

            dialog.dismiss()
        }
        dialog.show()
    }

    // Firestore document update function based on Firebase documentation
    // https://firebase.google.com/docs/firestore/manage-data/add-data
    private fun addNewCollection(newCollectionName: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                val newCollection = mapOf(newCollectionName to listOf<String>())

                val updatedCollection = favoriteCollection?.toMutableList() ?: mutableListOf()
                updatedCollection.add(newCollection)

                userRef.update("favoriteCollection", updatedCollection).addOnSuccessListener {
                    fetchFavoriteCollections()
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }
}

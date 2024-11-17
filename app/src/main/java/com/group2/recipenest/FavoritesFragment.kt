/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import FavoriteCollectionsTileModel
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
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
        // https://youtu.be/Mc0XT58A1Z4?si=CflzfraJ1Kckzmy3
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
        // Initializing itemTouchHelper to handle swipe gestures for Recycleview items
        // https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper
        // https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false
            // Handle the action when the item is swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val tile = adapter.getTileAtPosition(position)

                tile?.let {
                    showDeleteConfirmationDialog(it, position)
                } ?: run {
                    adapter.notifyItemChanged(position)
                }
            }
            // Added a background color to the swipe action
            // https://stackoverflow.com/questions/35773384/call-itemtouchhelper-onchilddraw-manually-to-swipe-item-on-recyclerview
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val itemView = viewHolder.itemView
                    val background = ColorDrawable(Color.RED)

                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )

                    background.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        // Attach the ItemTouchHelper to the RecyclerView to enable swipe functionality
        itemTouchHelper.attachToRecyclerView(recyclerView)

        fetchFavoriteCollections()

        // Toolbar title setup and customization based on Android developer documentation
        // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
        // https://developer.android.com/reference/android/content/res/Resources
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
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
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
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
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
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-mutable-list.html
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
    // Displays a confirmation dialog to the user when they attempt to delete a collection
    // https://developer.android.com/develop/ui/views/components/dialogs
    private fun showDeleteConfirmationDialog(tile: FavoriteCollectionsTileModel, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Collection")
            .setMessage("Are you sure you want to delete the ${tile.title} collection?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCollection(tile, position)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }.create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                    getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                }
            }
            .show()
    }
    // Deletes the user's favorite collection from firebase and updates the UI accordingly
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/filter-not.html
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-mutable-list.html
    private fun deleteCollection(tile: FavoriteCollectionsTileModel, position: Int) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? MutableList<Map<String, List<String>>>
                val updatedCollection = favoriteCollection?.filterNot { it.containsKey(tile.title.lowercase()) }

                userRef.update("favoriteCollection", updatedCollection).addOnSuccessListener {
                    val updatedTileList = adapter.getTileList().toMutableList()
                    updatedTileList.removeAt(position)
                    adapter.updateTiles(updatedTileList)
                }.addOnFailureListener { exception ->
                    Log.e("FavoritesFragment", "Error deleting collection", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FavoritesFragment", "Error fetching user data", exception)
        }
    }
}

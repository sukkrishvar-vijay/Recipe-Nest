/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipeCardModel
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class RecipeCardsFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tileTitle: String

    private var currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recipes_collections, container, false)

        tileTitle = arguments?.getString("tileTitle") ?: "Recipes"

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        toolbar.title = tileTitle
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        firestore = Firebase.firestore

        // RecyclerView setup and LinearLayoutManager usage based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        // https://stackoverflow.com/questions/50171647/recyclerview-setlayoutmanager
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Attach ItemTouchHelper for swipe-to-delete functionality
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipe = recipeAdapter.getRecipeAtPosition(position)
                recipe?.let {
                    showDeleteConfirmationDialog(it, position)
                } ?: run {
                    recipeAdapter.notifyItemChanged(position) // Reset swipe state if recipe is null
                }
            }

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

                    // Set the background bounds to match the full width of the item
                    background.setBounds(
                        itemView.left,        // Left edge of the item
                        itemView.top,         // Top edge
                        itemView.right,       // Right edge of the item (fixed, regardless of swipe distance)
                        itemView.bottom       // Bottom edge
                    )
                    background.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recipeRecyclerView)

        fetchRecipeIdsFromFavorites(tileTitle)

        return view
    }

    private fun showDeleteConfirmationDialog(recipe: RecipeCardModel, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRecipe(recipe, position)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                recipeAdapter.notifyItemChanged(position) // Reset swipe if deletion is canceled
            }.create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                    getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                }
            }
            .show()
    }

    private fun deleteRecipe(recipe: RecipeCardModel, position: Int) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? MutableList<Map<String, MutableList<String>>>
                var recipeRemoved = false

                // Locate and remove the recipe ID from the selected collection
                favoriteCollection?.forEach { collection ->
                    collection[tileTitle.lowercase()]?.let { recipeList ->
                        if (recipeList.contains(recipe.recipeId)) {
                            recipeList.remove(recipe.recipeId)
                            recipeRemoved = true
                        }
                    }
                }

                if (recipeRemoved) {
                    // Update Firestore and UI if recipe ID was removed
                    userRef.update("favoriteCollection", favoriteCollection).addOnSuccessListener {
                        recipeAdapter.removeRecipeAtPosition(position)
                        Log.d("RecipeCardsFragment", "Recipe successfully deleted from Firestore.")
                    }.addOnFailureListener { exception ->
                        Log.e("RecipeCardsFragment", "Failed to update Firestore: ${exception.message}")
                    }
                } else {
                    recipeAdapter.notifyItemChanged(position) // Reset swipe if not found
                    Log.e("RecipeCardsFragment", "Recipe ID not found in favoriteCollection.")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("RecipeCardsFragment", "Failed to retrieve user document: ${exception.message}")
        }
    }

    // Firestore document retrieval and querying based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    private fun fetchRecipeIdsFromFavorites(tileTitle: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                favoriteCollection?.let { collectionList ->
                    for (collectionMap in collectionList) {
                        for ((key, recipeIds) in collectionMap) {
                            if (key.equals(tileTitle, ignoreCase = true)) {
                                fetchRecipesByDocumentIds(recipeIds)
                                return@addOnSuccessListener
                            }
                        }
                    }
                    showEmptyState()
                }
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun fetchRecipesByDocumentIds(recipeIds: List<String>) {
        if (recipeIds.isEmpty()) {
            showEmptyState()
            return
        }

        val recipeList = mutableListOf<RecipeCardModel>()

        // Firestore document retrieval adapted from Firebase documentation
        // https://firebase.google.com/docs/firestore/query-data/get-data
        for (recipeId in recipeIds) {
            firestore.collection("Recipes").document(recipeId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                        val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                        val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                        val difficultyLevel = document.getString("difficultyLevel") ?: ""
                        val cuisineTypeList = document.get("cuisineType") as? List<String>
                        val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                        val recipeDescription = document.getString("recipeDescription") ?: ""
                        val recipeUserId = document.getString("recipeUserId") ?: ""
                        val dateRecipeAdded = document.getDate("dateRecipeAdded") ?: Date()
                        val recipeImageUrl = document.getString("recipeImageUrl") ?: ""
                        val recipeId = document.id

                        val recipe = RecipeCardModel(
                            recipeDescription = recipeDescription,
                            recipeUserId = recipeUserId,
                            recipeTitle = recipeTitle,
                            cookingTime = cookingTime,
                            avgRating = avgRating.toDouble(),
                            recipeImageUrl = recipeImageUrl,
                            difficultyLevel = difficultyLevel,
                            cuisineType = cuisineType,
                            recipeId = recipeId,
                            dateRecipeAdded = dateRecipeAdded
                        )

                        recipe.recipeId = recipeId
                        recipeList.add(recipe)

                        recipeAdapter = RecipeCardsAdapter(recipeList) { recipe ->
                            navigateToRecipeDetailsFragment(recipe)
                        }
                        recipeRecyclerView.adapter = recipeAdapter
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    private fun showEmptyState() {
    }

    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val recipeDetailsFragment = RecipeDetailsFragment()

        // Passing data between fragments using Bundle based on Android developer documentation
        // https://developer.android.com/guide/fragments/communicate
        // https://stackoverflow.com/questions/16499385/using-bundle-to-pass-data-between-fragment-to-another-fragment-example
        val bundle = Bundle()
        bundle.putString("recipeUserId", recipe.recipeUserId)
        bundle.putString("recipeDescription", recipe.recipeDescription)
        bundle.putString("recipeTitle", recipe.recipeTitle)
        bundle.putString("avgRating", recipe.avgRating.toString())
        bundle.putString("difficultyLevel", recipe.difficultyLevel)
        bundle.putInt("cookingTime", recipe.cookingTime)
        bundle.putString("cuisineType", recipe.cuisineType)
        bundle.putString("recipeId", recipe.recipeId)
        bundle.putString("recipeImageUrl", recipe.recipeImageUrl)

        recipeDetailsFragment.arguments = bundle

        // Fragment navigation and transaction pattern adapted from Android developer documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        // https://medium.com/@zorbeytorunoglu/fragment-navigation-on-android-c45488184399
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    // Using companion object and factory method to pass arguments in a fragment based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate#fragment-create
    // https://medium.com/@pablisco/companion-factory-methods-in-kotlin-e2eeb1e87f1b
    companion object {
        fun newInstance(tileTitle: String): RecipeCardsFragment {
            val fragment = RecipeCardsFragment()
            val args = Bundle()
            args.putString("tileTitle", tileTitle)
            fragment.arguments = args
            return fragment
        }
    }
}

/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
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
import android.widget.TextView
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
    private lateinit var emptyStateTextView: TextView

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

        emptyStateTextView = view.findViewById(R.id.no_recipes_text_view)

        // RecyclerView setup and LinearLayoutManager usage based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        // https://stackoverflow.com/questions/50171647/recyclerview-setlayoutmanager
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initializing itemTouchHelper to handle swipe gestures for Recycleview items
        // https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper
        // https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipe = recipeAdapter.getRecipeAtPosition(position)
                recipe?.let {
                    showDeleteConfirmationDialog(it, position)
                } ?: run {
                    recipeAdapter.notifyItemChanged(position)
                }
            }
            // Add a background color to the swipe action
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
        // https://www.youtube.com/watch?v=uvzP8KTz4Fg&ab_channel=CodingWithMitch
        itemTouchHelper.attachToRecyclerView(recipeRecyclerView)

        fetchRecipeIdsFromFavorites(tileTitle)

        return view
    }
    // Displays a confirmation dialog to the user when they attempt to delete a collection
    // https://developer.android.com/develop/ui/views/components/dialogs
    private fun showDeleteConfirmationDialog(recipe: RecipeCardModel, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to remove this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRecipe(recipe, position)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                recipeAdapter.notifyItemChanged(position)
            }.create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                    getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                }
            }
            .show()
    }
    // Deletes the user's favorite collection or user's Recipe from firebase and updates the UI accordingly
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-mutable-list.html
    private fun deleteRecipe(recipe: RecipeCardModel, position: Int) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? MutableList<Map<String, MutableList<String>>>
                var recipeRemoved = false

                favoriteCollection?.forEach { collection ->
                    collection[tileTitle.lowercase()]?.let { recipeList ->
                        if (recipeList.contains(recipe.recipeId)) {
                            recipeList.remove(recipe.recipeId)
                            recipeRemoved = true
                        }
                    }
                }

                if (recipeRemoved) {
                    userRef.update("favoriteCollection", favoriteCollection).addOnSuccessListener {
                        recipeAdapter.removeRecipeAtPosition(position)
                        if (recipeAdapter.itemCount == 0) {
                            showEmptyState()
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("RecipeCardsFragment", "Failed to update Firestore: ${exception.message}")
                    }
                } else {
                    recipeAdapter.notifyItemChanged(position)
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
                        emptyStateTextView.visibility = View.GONE
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    private fun showEmptyState() {
        emptyStateTextView.visibility = View.VISIBLE
        emptyStateTextView.text = "No recipes added to this collection!"
        recipeRecyclerView.visibility = View.GONE
    }

    // Navigates to the recipe details fragment with selected recipe data
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

package com.group2.recipenest

import RecipeCardModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecipeCardsFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore

    // User ID to filter recipes (based on passed userId, adjust as needed)
    private var currentUserId = "ceZ4r5FauC7TuTyckeRp"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recipes_collections, container, false)

        // Get the tile title passed from FavoritesFragment
        val tileTitle = arguments?.getString("tileTitle") ?: "Recipes"

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title to the tile title passed from FavoritesFragment
        toolbar.title = tileTitle
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set up the back button (up button)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()  // Navigate back when back button is clicked
        }

        // Initialize Firestore
        firestore = Firebase.firestore

        // Initialize RecyclerView
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch recipe IDs from the favoriteCollection
        fetchRecipeIdsFromFavorites(tileTitle)

        return view
    }

    // Function to fetch recipe IDs from favoriteCollection in User collection
    private fun fetchRecipeIdsFromFavorites(tileTitle: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                favoriteCollection?.let { collectionList ->
                    // Loop through the list of maps to find the matching key (category)
                    for (collectionMap in collectionList) {
                        for ((key, recipeIds) in collectionMap) {
                            // Check if the key (category) matches the tileTitle
                            if (key.equals(tileTitle, ignoreCase = true)) {
                                // Fetch recipes for the matching recipe IDs
                                fetchRecipesByDocumentIds(recipeIds)
                                return@addOnSuccessListener
                            }
                        }
                    }
                    // Handle case where no matching key was found
                    showEmptyState()
                }
            }
        }.addOnFailureListener {
            // Handle error in fetching the favoriteCollection
            it.printStackTrace()
        }
    }

    // Function to fetch recipes from Firestore by their document IDs
    private fun fetchRecipesByDocumentIds(recipeIds: List<String>) {
        if (recipeIds.isEmpty()) {
            showEmptyState()
            return
        }

        val recipeList = mutableListOf<RecipeCardModel>()

        // For each recipe ID, query the Firestore document directly
        for (recipeId in recipeIds) {
            firestore.collection("Recipes").document(recipeId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Safely retrieve each field from Firestore document
                        val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                        val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                        val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                        val difficultyLevel = document.getString("difficultyLevel") ?: ""
                        val cuisineTypeList = document.get("cuisineType") as? List<String>
                        val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                        val recipeDescription = document.getString("recipeDescription") ?: ""
                        val recipeUserId = document.getString("recipeUserId") ?: ""
                        val recipeId = document.id

                        // Create a RecipeCardModel object and add it to the list
                        val recipe = RecipeCardModel(
                            recipeDescription = recipeDescription,
                            recipeUserId = recipeUserId,
                            recipeTitle = recipeTitle,
                            cookingTime = cookingTime,
                            avgRating = avgRating,
                            imageResId = R.drawable.placeholder_recipe_image,
                            difficultyLevel = difficultyLevel,
                            cuisineType = cuisineType,
                            recipeId = recipeId
                        )

                        // Add document ID (recipeId) to the recipe model
                        recipe.recipeId = recipeId
                        recipeList.add(recipe)

                        // Update the adapter once all recipes are fetched
                        recipeAdapter = RecipeCardsAdapter(recipeList) { recipe ->
                            navigateToRecipeDetailsFragment(recipe)
                        }
                        recipeRecyclerView.adapter = recipeAdapter
                    }
                }.addOnFailureListener { exception ->
                    // Handle any failure in fetching a recipe document
                    exception.printStackTrace()
                }
        }
    }

    // Function to show an empty state if no recipes found
    private fun showEmptyState() {
        // You can show a placeholder or a message in case there are no recipes found.
    }

    // Navigate to RecipeDetailsFragment and pass the recipe document
    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val recipeDetailsFragment = RecipeDetailsFragment()

        // Pass recipe details to the fragment using a bundle
        val bundle = Bundle()
        bundle.putString("recipeUserId", recipe.recipeUserId)
        bundle.putString("recipeDescription", recipe.recipeDescription)
        bundle.putString("recipeTitle", recipe.recipeTitle)
        bundle.putString("avgRating", recipe.avgRating.toString())
        bundle.putString("difficultyLevel", recipe.difficultyLevel)
        bundle.putInt("cookingTime", recipe.cookingTime)
        bundle.putString("cuisineType", recipe.cuisineType)
        bundle.putString("recipeId", recipe.recipeId)  // Pass the document ID (recipeId)

        recipeDetailsFragment.arguments = bundle

        // Navigate to RecipeDetailsFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        // Method to create a new instance of RecipeCardsFragment and pass the tile title
        fun newInstance(tileTitle: String): RecipeCardsFragment {
            val fragment = RecipeCardsFragment()
            val args = Bundle()
            args.putString("tileTitle", tileTitle)
            fragment.arguments = args
            return fragment
        }
    }
}

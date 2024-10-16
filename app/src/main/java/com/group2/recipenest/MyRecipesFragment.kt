package com.group2.recipenest

import AddRecipeFragment
import RecipeCardModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyRecipesFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore

    // User ID to filter recipes
    private val recipeUserId = "ceZ4r5FauC7TuTyckeRp"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_recipes_collection, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title
        toolbar.title = "My Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set up the back button (up button)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Handle FAB click to navigate to AddRecipeFragment
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_new_recipe)
        fab.setOnClickListener {
            val addRecipeFragment = AddRecipeFragment()

            // Navigate to AddRecipeFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addRecipeFragment)
                .addToBackStack(null)
                .commit()
        }

        // Initialize RecyclerView
        recipeRecyclerView = view.findViewById(R.id.my_recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch recipes from Firestore
        fetchUserRecipes()

        return view
    }

    // Function to fetch user-specific recipes from Firestore
    private fun fetchUserRecipes() {
        firestore.collection("Recipes")
            .whereEqualTo("recipeUserId", recipeUserId)
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()

                for (document in documents) {
                    // Safely retrieve each field from Firestore document
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"

                    // Create a RecipeCardModel object and add it to the list
                    val recipe = RecipeCardModel(
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = R.drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType
                    )
                    recipeList.add(recipe)
                }

                // Set up the adapter with the fetched recipes
                recipeAdapter = RecipeCardsAdapter(recipeList)
                recipeRecyclerView.adapter = recipeAdapter
            }
            .addOnFailureListener { exception ->
                // Handle error case here
                exception.printStackTrace()
            }
    }
}

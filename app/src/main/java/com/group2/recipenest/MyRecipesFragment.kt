package com.group2.recipenest

import AddRecipeFragment
import RecipeCardModel
import android.os.Bundle
import android.util.Log
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
    private var currentUserId = "ceZ4r5FauC7TuTyckeRp"

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

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh the data every time the fragment is resumed
        fetchUserRecipes()
    }

    // Function to fetch user-specific recipes from Firestore
    private fun fetchUserRecipes() {
        firestore.collection("Recipes")
            .whereEqualTo("recipeUserId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()

                for (document in documents) {
                    // Safely retrieve each field from Firestore document
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    Log.d("Recipes Description", recipeDescription)
                    val recipeUserId = document.getString("recipeUserId") ?: ""

                    // Create a RecipeCardModel object and add it to the list
                    val recipe = RecipeCardModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = R.drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType
                    )
                    recipeList.add(recipe)
                }

                // Set up the adapter with the fetched recipes and handle item click
                recipeAdapter = RecipeCardsAdapter(recipeList) { recipe ->
                    navigateToRecipeDetailsFragment(recipe)
                }
                recipeRecyclerView.adapter = recipeAdapter
            }
            .addOnFailureListener { exception ->
                // Handle error case here
                exception.printStackTrace()
            }
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

        recipeDetailsFragment.arguments = bundle

        // Navigate to RecipeDetailsFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}

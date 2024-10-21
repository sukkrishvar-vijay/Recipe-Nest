package com.group2.recipenest

import RecipeCardModel
import TrendingRecipeCardsAdapter
import TrendingRecipeCardsModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecipesFragment : Fragment() {

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verticalAdapter: RecipeCardsAdapter
    private lateinit var horizontalAdapter: TrendingRecipeCardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_recipe, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Hide the Toolbar
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Set up the horizontal RecyclerView for "Recipes Trending Locally"
        horizontalRecyclerView = rootView.findViewById(R.id.horizontalRecyclerView)
        horizontalRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Set up the horizontal adapter
        horizontalAdapter = TrendingRecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        horizontalRecyclerView.adapter = horizontalAdapter

        // Set up the vertical RecyclerView for "More Recipes"
        verticalRecyclerView = rootView.findViewById(R.id.verticalRecyclerView)
        verticalRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the vertical adapter with an empty list
        verticalAdapter = RecipeCardsAdapter(listOf()) { recipe ->
            // Handle recipe card click, e.g., navigate to RecipeDetailsFragment
            navigateToRecipeDetailsFragment(recipe)
        }
        verticalRecyclerView.adapter = verticalAdapter

        // Fetch recipes from Firestore for both vertical and horizontal lists
        fetchRecipesFromFirestore()

        return rootView
    }

    // Function to fetch recipes from Firestore and update the vertical and horizontal lists
    private fun fetchRecipesFromFirestore() {
        firestore.collection("Recipes")
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()
                val recipeList2 = mutableListOf<TrendingRecipeCardsModel>()

                for (document in documents) {
                    // Safely retrieve each field from Firestore document
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    val recipeUserId = document.getString("recipeUserId") ?: ""
                    val recipeId = document.id

                    // Create a RecipeCardModel object and add it to the vertical list
                    val recipe = RecipeCardModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = R.drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId
                    )

                    // Create a TrendingRecipeCardsModel object and add it to the horizontal list
                    val recipeAnother = TrendingRecipeCardsModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = R.drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId
                    )

                    recipeList.add(recipe)
                    recipeList2.add(recipeAnother)
                }

                // Update the vertical and horizontal adapters with the fetched recipes
                verticalAdapter.updateRecipes(recipeList)
                horizontalAdapter.updateTrendingRecipes(recipeList2)
            }
            .addOnFailureListener { exception ->
                // Handle the error if fetching the data fails
                exception.printStackTrace()
            }
    }

    // Navigate to RecipeDetailsFragment and pass the recipe document including the recipeId
    private fun navigateToRecipeDetailsFragment(recipe: Any) {
        val recipeDetailsFragment = RecipeDetailsFragment()

        // Create a bundle to pass the recipe details to the RecipeDetailsFragment
        val bundle = Bundle()

        // Use 'when' to check if recipe is RecipeCardModel or TrendingRecipeCardsModel
        when (recipe) {
            is RecipeCardModel -> {
                bundle.putString("recipeUserId", recipe.recipeUserId)
                bundle.putString("recipeDescription", recipe.recipeDescription)
                bundle.putString("recipeTitle", recipe.recipeTitle)
                bundle.putString("avgRating", recipe.avgRating.toString())
                bundle.putString("difficultyLevel", recipe.difficultyLevel)
                bundle.putInt("cookingTime", recipe.cookingTime)
                bundle.putString("cuisineType", recipe.cuisineType)
                bundle.putString("recipeId", recipe.recipeId)
            }
            is TrendingRecipeCardsModel -> {
                bundle.putString("recipeUserId", recipe.recipeUserId)
                bundle.putString("recipeDescription", recipe.recipeDescription)
                bundle.putString("recipeTitle", recipe.recipeTitle)
                bundle.putString("avgRating", recipe.avgRating.toString())
                bundle.putString("difficultyLevel", recipe.difficultyLevel)
                bundle.putInt("cookingTime", recipe.cookingTime)
                bundle.putString("cuisineType", recipe.cuisineType)
                bundle.putString("recipeId", recipe.recipeId)
            }
            else -> {
                // Handle the case where the recipe object is not recognized
                return
            }
        }

        recipeDetailsFragment.arguments = bundle

        // Navigate to RecipeDetailsFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the Toolbar again when the fragment is destroyed
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    // Hide the toolbar again when the fragment is resumed
    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
}

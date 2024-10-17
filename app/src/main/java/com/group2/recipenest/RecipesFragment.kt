package com.group2.recipenest

import RecipeCardModel
import RecipesCarouselAdapter
import RecipesCarouselModel
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
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecipesFragment : Fragment() {

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private lateinit var carouselViewPager: ViewPager2
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verticalAdapter: RecipeCardsAdapter

    // User ID to filter recipes
    private lateinit var recipeUserId: String

    private lateinit var recipeDescription: String

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

        // Sample data for the carousel
        val carouselItems = listOf(
            RecipesCarouselModel("Recipe 1", R.drawable.placeholder_recipe_image),
            RecipesCarouselModel("Recipe 2", R.drawable.placeholder_recipe_image),
            RecipesCarouselModel("Recipe 3", R.drawable.placeholder_recipe_image)
        )

        // Set up the carousel (ViewPager2)
        carouselViewPager = rootView.findViewById(R.id.carouselViewPager)
        val carouselAdapter = RecipesCarouselAdapter(carouselItems) // Assume this adapter is implemented
        carouselViewPager.adapter = carouselAdapter

        // Set up the horizontal RecyclerView for "Recipes Trending Locally"
        horizontalRecyclerView = rootView.findViewById(R.id.horizontalRecyclerView)
        horizontalRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Sample data for the horizontal list
        val trendingRecipes = listOf(
            TrendingRecipeCardsModel("Recipe 1", "Easy • 30mins • Thai", "4.5★", R.drawable.placeholder_recipe_image),
            TrendingRecipeCardsModel("Recipe 2", "Medium • 45mins • Italian", "4.7★", R.drawable.placeholder_recipe_image),
            TrendingRecipeCardsModel("Recipe 3", "Hard • 60mins • French", "4.9★", R.drawable.placeholder_recipe_image)
        )

        // Set up the horizontal adapter
        val horizontalAdapter = TrendingRecipeCardsAdapter(trendingRecipes)
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

        // Fetch recipes from Firestore for vertical list
        fetchRecipesFromFirestore()

        return rootView
    }

    // Function to fetch recipes from Firestore and update the vertical list
    private fun fetchRecipesFromFirestore() {
        firestore.collection("Recipes")
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()
                for (document in documents) {
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    recipeDescription = document.getString("recipeDescription") ?:""
                    recipeUserId = document.getString("recipeUserId") ?:""

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

                // Update the vertical adapter with the fetched recipes
                verticalAdapter.updateRecipes(recipeList)
            }
            .addOnFailureListener { exception ->
                // Handle the error if fetching the data fails
                exception.printStackTrace()
            }
    }

    // Navigate to RecipeDetailsFragment (this method can be customized)
    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val recipeDetailsFragment = RecipeDetailsFragment()

        val bundle = Bundle()
        bundle.putString("recipeUserId", recipeUserId)
        bundle.putString("recipeDescription", recipeDescription)
        bundle.putString("recipeTitle", recipe.recipeTitle)
        bundle.putString("avgRating", recipe.avgRating.toString())
        bundle.putString("difficultyLevel", recipe.difficultyLevel)
        bundle.putInt("cookingTime", recipe.cookingTime)
        bundle.putString("cuisineType", recipe.cuisineType)

        recipeDetailsFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the Toolbar again when the fragment is destroyed
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}

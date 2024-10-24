package com.group2.recipenest

import RecipeCardModel
import RecipesCarouselModel
import TrendingRecipeCardsAdapter
import TrendingRecipeCardsModel
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.group2.recipenest.R.*

class RecipesFragment : Fragment() {

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private lateinit var carouselRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verticalAdapter: RecipeCardsAdapter
    private lateinit var horizontalAdapter: TrendingRecipeCardsAdapter
    private lateinit var carouselAdapter: RecipesCarouselAdapter
    private val handler = Handler(Looper.getMainLooper()) // Handler for auto-scrolling
    private var scrollPosition = 0
    private var carouselRecipeList: List<RecipesCarouselModel> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(layout.fragment_recipe, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Hide the Toolbar
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Set up the RecyclerView for the carousel
        carouselRecyclerView = rootView.findViewById(R.id.carouselViewPager)
        carouselAdapter = RecipesCarouselAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }

        // Set up the CarouselLayoutManager with the Hero strategy
        val carouselLayoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        carouselRecyclerView.layoutManager = carouselLayoutManager
        carouselRecyclerView.adapter = carouselAdapter

        // Start continuous auto-scrolling
        startContinuousAutoScroll()

        // Set up the horizontal RecyclerView for "Recipes Trending Locally"
        horizontalRecyclerView = rootView.findViewById(R.id.horizontalRecyclerView)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        horizontalAdapter = TrendingRecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        horizontalRecyclerView.adapter = horizontalAdapter

        // Set up the vertical RecyclerView for "More Recipes"
        verticalRecyclerView = rootView.findViewById(R.id.verticalRecyclerView)
        verticalRecyclerView.layoutManager = LinearLayoutManager(context)
        verticalAdapter = RecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        verticalRecyclerView.adapter = verticalAdapter

        // Fetch recipes from Firestore for both vertical, horizontal, and carousel lists
        fetchRecipesFromFirestore()

        return rootView
    }

    // Function to fetch recipes from Firestore and update the vertical, horizontal, and carousel lists
    private fun fetchRecipesFromFirestore() {
        firestore.collection("Recipes")
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()  // For vertical RecyclerView
                val trendingRecipeList = mutableListOf<TrendingRecipeCardsModel>()  // For horizontal RecyclerView
                val carouselRecipeListOriginal = mutableListOf<RecipesCarouselModel>()  // For carousel

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

                    // Add to vertical list (RecipeCardModel)
                    val recipe = RecipeCardModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId
                    )
                    recipeList.add(recipe)

                    // Add to horizontal list (TrendingRecipeCardsModel)
                    val trendingRecipe = TrendingRecipeCardsModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId
                    )
                    trendingRecipeList.add(trendingRecipe)

                    // Add to carousel list (RecipesCarouselModel)
                    val carouselRecipe = RecipesCarouselModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId
                    )
                    carouselRecipeListOriginal.add(carouselRecipe)
                }

                // Duplicate carousel list for smooth infinite scrolling
                carouselRecipeList = carouselRecipeListOriginal + carouselRecipeListOriginal

                // Update the adapters
                verticalAdapter.updateRecipes(recipeList)  // Update the vertical RecyclerView
                horizontalAdapter.updateTrendingRecipes(trendingRecipeList)  // Update the horizontal RecyclerView
                carouselAdapter.updateCarouselItems(carouselRecipeList)  // Update the carousel RecyclerView
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during Firestore fetch
                exception.printStackTrace()
            }
    }

    // Function to start continuous auto-scrolling the carousel
    private fun startContinuousAutoScroll() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                scrollPosition = (scrollPosition + 1) % carouselAdapter.itemCount
                carouselRecyclerView.smoothScrollToPosition(scrollPosition)

                // Reset position to simulate infinite scrolling
                if (scrollPosition == carouselRecipeList.size / 2) {
                    scrollPosition = 0
                    carouselRecyclerView.scrollToPosition(0)
                }

                handler.postDelayed(this, 2000)  // Scroll every 2 seconds
            }
        }, 2000)  // Initial delay
    }

    // Function to stop auto-scrolling when fragment is destroyed
    private fun stopAutoScroll() {
        handler.removeCallbacksAndMessages(null)
    }

    // Handle navigation to details for different models
    private fun navigateToRecipeDetailsFragment(recipe: Any) {
        val recipeDetailsFragment = RecipeDetailsFragment()
        val bundle = Bundle()

        // Handle RecipeCardModel, TrendingRecipeCardsModel, and RecipesCarouselModel types
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
            is RecipesCarouselModel -> {
                bundle.putString("recipeUserId", recipe.recipeUserId)
                bundle.putString("recipeDescription", recipe.recipeDescription)
                bundle.putString("recipeTitle", recipe.recipeTitle)
                bundle.putString("avgRating", recipe.avgRating.toString())
                bundle.putString("difficultyLevel", recipe.difficultyLevel)
                bundle.putInt("cookingTime", recipe.cookingTime)
                bundle.putString("cuisineType", recipe.cuisineType)
                bundle.putString("recipeId", recipe.recipeId)
            }
            else -> return
        }

        // Pass the bundle with the recipe details to RecipeDetailsFragment
        recipeDetailsFragment.arguments = bundle

        // Navigate to RecipeDetailsFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()  // Stop auto-scrolling when the view is destroyed
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
}
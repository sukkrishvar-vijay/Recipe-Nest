/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipeCardModel
import RecipesCarouselModel
import TrendingRecipeCardsModel
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group2.recipenest.R.*
import java.util.Date

class RecipesFragment : Fragment() {

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private lateinit var carouselRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verticalAdapter: RecipeCardsAdapter
    private lateinit var horizontalAdapter: TrendingRecipeCardsAdapter
    private lateinit var carouselAdapter: RecipesCarouselAdapter
    private lateinit var trendingTitle: TextView
    private lateinit var moreRecipesTitle: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0
    private var carouselRecipeList: List<RecipesCarouselModel> = listOf()
    private lateinit var locationHelper: LocationHelper
    private var currentLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(layout.fragment_recipe, container, false)

        firestore = Firebase.firestore

        (activity as AppCompatActivity).supportActionBar?.hide()

        // Carousel layout setup with HeroCarouselStrategy based on Material Design documentation
        // https://developer.android.com/reference/com/google/android/material/carousel/CarouselLayoutManager
        // https://youtu.be/dvaanTc24KY?si=5-gk_JOgy7wdi8CF
        carouselRecyclerView = rootView.findViewById(R.id.carouselViewPager)
        carouselAdapter = RecipesCarouselAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }

        val carouselLayoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        carouselRecyclerView.layoutManager = carouselLayoutManager
        carouselRecyclerView.adapter = carouselAdapter

        startContinuousAutoScroll()

        trendingTitle = rootView.findViewById(R.id.trendingTitle)
        locationHelper = LocationHelper(requireContext())

        locationHelper.getCityName { cityName ->
            currentLocation = cityName ?: "Location not found"
        }

        // RecyclerView setup with different layout managers based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        horizontalRecyclerView = rootView.findViewById(R.id.horizontalRecyclerView)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        horizontalAdapter = TrendingRecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        horizontalRecyclerView.adapter = horizontalAdapter

        // RecyclerView setup with different layout managers based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/list-of.html
        verticalRecyclerView = rootView.findViewById(R.id.verticalRecyclerView)
        verticalRecyclerView.layoutManager = LinearLayoutManager(context)
        verticalAdapter = RecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        verticalRecyclerView.adapter = verticalAdapter

        fetchRecipesFromFirestore()

        moreRecipesTitle = rootView.findViewById(R.id.moreRecipesTitle)
        moreRecipesTitle.text = "More Recipes"

        return rootView
    }

    // Firestore data retrieval and querying based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://developer.android.com/reference/com/google/android/play/core/tasks/OnSuccessListener
    private fun fetchRecipesFromFirestore() {
        firestore.collection("Recipes")
            .get()
            .addOnSuccessListener { documents ->

                // Retrieving Firestore documents and creating instances of multiple data models based on Firebase documentation
                // https://firebase.google.com/docs/firestore/query-data/get-data
                // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
                val recipeList = mutableListOf<RecipeCardModel>()
                val trendingRecipeList = mutableListOf<TrendingRecipeCardsModel>()
                val carouselRecipeListOriginal = mutableListOf<RecipesCarouselModel>()

                for (document in documents) {
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    val recipeUserId = document.getString("recipeUserId") ?: ""
                    val dateRecipeAdded = document.getDate("dateRecipeAdded") ?: Date()
                    val recipeUploadLocation = document.getString("recipeUploadLocation") ?: ""
                    val recipeImageUrl = document.getString("recipeImageUrl") ?: ""
                    Log.d("ImageUrl", recipeImageUrl)
                    val recipeId = document.id

                    val recipe = RecipeCardModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating.toDouble(),
                        recipeImageUrl = recipeImageUrl,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId,
                        dateRecipeAdded = dateRecipeAdded
                    )
                    recipeList.add(recipe)

                    if (recipeUploadLocation.equals(currentLocation, ignoreCase = true)) {
                        val trendingRecipe = TrendingRecipeCardsModel(
                            recipeUserId = recipeUserId,
                            recipeDescription = recipeDescription,
                            recipeTitle = recipeTitle,
                            cookingTime = cookingTime,
                            avgRating = avgRating.toDouble(),
                            recipeImageUrl = recipeImageUrl,
                            difficultyLevel = difficultyLevel,
                            cuisineType = cuisineType,
                            recipeId = recipeId,
                            dateRecipeAdded = dateRecipeAdded
                        )
                        trendingRecipeList.add(trendingRecipe)
                    }

                    val carouselRecipe = RecipesCarouselModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating.toDouble(),
                        recipeImageUrl = recipeImageUrl,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId,
                        dateRecipeAdded = dateRecipeAdded
                    )
                    carouselRecipeListOriginal.add(carouselRecipe)
                }

                val topCarouselRecipes = carouselRecipeListOriginal
                    .sortedWith(compareByDescending<RecipesCarouselModel> { it.avgRating.toDouble() }
                        .thenByDescending { it.dateRecipeAdded })
                    .take(5)

                carouselRecipeList = topCarouselRecipes + topCarouselRecipes

                val sortedRecipeList = recipeList.sortedByDescending { it.dateRecipeAdded }

                verticalAdapter.updateRecipes(sortedRecipeList)
                if (trendingRecipeList.isEmpty()){
                    trendingTitle.visibility = View.GONE
                }else{
                    trendingTitle.visibility = View.VISIBLE
                    horizontalAdapter.updateTrendingRecipes(trendingRecipeList)
                    trendingTitle.text = "Recipes Trending in ${currentLocation}"
                }
                carouselAdapter.updateCarouselItems(carouselRecipeList)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Auto-scrolling RecyclerView carousel pattern based on Android developer community examples
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
    private fun startContinuousAutoScroll() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                scrollPosition = (scrollPosition + 1) % carouselAdapter.itemCount
                carouselRecyclerView.smoothScrollToPosition(scrollPosition)

                if (scrollPosition == carouselRecipeList.size / 2) {
                    scrollPosition = 0
                    carouselRecyclerView.scrollToPosition(0)
                }

                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun stopAutoScroll() {
        handler.removeCallbacksAndMessages(null)
    }

    // Passing data between fragments using Bundle based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    // https://developer.android.com/reference/kotlin/android/os/Bundle
    private fun navigateToRecipeDetailsFragment(recipe: Any) {
        val recipeDetailsFragment = RecipeDetailsFragment()
        val bundle = Bundle()

        // Handling multiple data models with `when` expression and passing data using Bundle based on Android developer documentation
        // https://developer.android.com/guide/fragments/communicate
        // https://developer.android.com/reference/kotlin/android/os/Bundle
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
                bundle.putString("recipeImageUrl", recipe.recipeImageUrl)
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
                bundle.putString("recipeImageUrl", recipe.recipeImageUrl)
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
                bundle.putString("recipeImageUrl", recipe.recipeImageUrl)
            }
            else -> return
        }

        recipeDetailsFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    // Controlling ActionBar visibility in fragments based on Android developer documentation
    // https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity#getSupportActionBar()
    // https://developer.android.com/reference/android/app/ActionBar
    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    // Controlling ActionBar visibility in fragments based on Android developer documentation
    // https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity#getSupportActionBar()
    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
}
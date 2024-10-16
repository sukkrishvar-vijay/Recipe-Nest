package com.group2.recipenest

import RecipeCardModel
import RecipesCarouselAdapter
import RecipesCarouselModel
import TrendingRecipeCardsAdapter
import TrendingRecipeCardsModel
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
import androidx.viewpager2.widget.ViewPager2

class RecipesFragment : Fragment() {

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private lateinit var carouselViewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_recipe, container, false)

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
//        val carouselAdapter = RecipesCarouselAdapter(carouselItems)
//        carouselViewPager.adapter = carouselAdapter

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

        // Sample data for the vertical list
        val moreRecipes = listOf(
            RecipeCardModel("Recipe 4", "Easy • 20mins • Mexican", "4.3★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 5", "Hard • 50mins • Indian", "4.8★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 6", "Medium • 35mins • Chinese", "4.6★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 4", "Easy • 20mins • Mexican", "4.3★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 5", "Hard • 50mins • Indian", "4.8★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 6", "Medium • 35mins • Chinese", "4.6★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 4", "Easy • 20mins • Mexican", "4.3★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 5", "Hard • 50mins • Indian", "4.8★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 6", "Medium • 35mins • Chinese", "4.6★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 4", "Easy • 20mins • Mexican", "4.3★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 5", "Hard • 50mins • Indian", "4.8★", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 6", "Medium • 35mins • Chinese", "4.6★", R.drawable.placeholder_recipe_image)
        )

        // Set up the vertical adapter
        val verticalAdapter = RecipeCardsAdapter(moreRecipes)
        verticalRecyclerView.adapter = verticalAdapter

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the Toolbar again when the fragment is destroyed
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}

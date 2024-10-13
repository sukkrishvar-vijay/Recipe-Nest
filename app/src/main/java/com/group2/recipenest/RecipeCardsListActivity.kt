package com.group2.recipenest

import RecipeCardModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.group2.recipenest.R

class RecipeCardsListActivity:AppCompatActivity() {
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipes_collections)

//        // Find the toolbar in the activity
//        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
//
//        // Set the toolbar title directly
//        toolbar.title = ""
//        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Sample data
        val recipeList = listOf(
            RecipeCardModel("Recipe 1", "Easy • 30mins • Thai, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 2", "Medium • 45mins • Italian, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 3", "Hard • 60mins • Mexican", R.drawable.placeholder_recipe_image)
        )

        // Initialize RecyclerView
        recipeRecyclerView = findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeCardsAdapter(recipeList)
        recipeRecyclerView.adapter = recipeAdapter
    }
}
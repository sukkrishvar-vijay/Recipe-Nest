package com.group2.recipenest

import RecipeCardModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class RecipeCardsAdapter(
    private var recipeList: List<RecipeCardModel>, // Start with an empty or initial list
    private val onClick: (RecipeCardModel) -> Unit // Callback for when an item is clicked
) : RecyclerView.Adapter<RecipeCardsAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.bind(recipe, onClick)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    // ViewHolder class to hold each item view
    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        private val recipeImage: ImageView = itemView.findViewById(R.id.image_container)
        private val recipeRating: TextView = itemView.findViewById(R.id.recipe_rating)

        fun bind(recipe: RecipeCardModel, onClick: (RecipeCardModel) -> Unit) {
            recipeTitle.text = recipe.recipeTitle
            recipeRating.text = "${recipe.avgRating}★"
            recipeDescription.text = "${recipe.difficultyLevel} • ${recipe.cookingTime} mins\n${recipe.cuisineType}"
            recipeImage.setImageResource(recipe.imageResId)

            // Handle click on the recipe card
            itemView.setOnClickListener {
                onClick(recipe) // Trigger the callback with the clicked recipe
            }
        }
    }

    // Method to update the list of recipes and notify the adapter of data changes
    fun updateRecipes(newRecipes: List<RecipeCardModel>) {
        recipeList = newRecipes
        notifyDataSetChanged() // Notify RecyclerView to refresh the UI
    }
}

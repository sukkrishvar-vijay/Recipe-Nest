/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import TrendingRecipeCardsModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

// Setting up RecyclerView Adapter and ViewHolder based on Android developer documentation
// https://developer.android.com/guide/topics/ui/layout/recyclerview
// https://learn.microsoft.com/en-us/dotnet/api/android.widget.imageview.setimageresource?view=net-android-34.0
class TrendingRecipeCardsAdapter(
    private var recipes: List<TrendingRecipeCardsModel>,
    private val onClick: (TrendingRecipeCardsModel) -> Unit
) : RecyclerView.Adapter<TrendingRecipeCardsAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    // Binding data to ViewHolder based on Android developer documentation
    // https://developer.android.com/guide/topics/ui/layout/recyclerview#bind-data

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeImageView.setImageResource(recipe.imageResId)
        holder.recipeTitleTextView.text = recipe.recipeTitle
        holder.recipeDetailsTextView.text = "${recipe.difficultyLevel} • ${recipe.cookingTime} mins • ${recipe.cuisineType}"
        holder.recipeRatingTextView.text = "${recipe.avgRating}★"

        // Setting item click listener for RecyclerView items based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/controls/button
        holder.itemView.setOnClickListener {
            onClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    // Updating RecyclerView data and notifying adapter based on Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifydatasetchanged
    fun updateTrendingRecipes(newRecipes: List<TrendingRecipeCardsModel>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    // ViewHolder setup to hold item view references in RecyclerView based on Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        val recipeTitleTextView: TextView = itemView.findViewById(R.id.recipeTitleTextView)
        val recipeDetailsTextView: TextView = itemView.findViewById(R.id.recipeDetailsTextView)
        val recipeRatingTextView: TextView = itemView.findViewById(R.id.recipeRatingTextView)
    }
}

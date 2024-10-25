/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipeCardModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

// RecyclerView Adapter and ViewHolder implementation based on Android developer documentation
// https://developer.android.com/guide/topics/ui/layout/recyclerview
class RecipeCardsAdapter(
    private var recipeList: List<RecipeCardModel>,
    private val onClick: (RecipeCardModel) -> Unit
) : RecyclerView.Adapter<RecipeCardsAdapter.RecipeViewHolder>() {

    // ViewHolder creation and view inflation based on Android developer guide
    // https://developer.android.com/guide/topics/ui/layout/recyclerview
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

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        private val recipeImage: ImageView = itemView.findViewById(R.id.image_container)
        private val recipeRating: TextView = itemView.findViewById(R.id.recipe_rating)

        // Data binding in RecyclerView ViewHolder adapted from Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview#bind-data
        fun bind(recipe: RecipeCardModel, onClick: (RecipeCardModel) -> Unit) {
            recipeTitle.text = recipe.recipeTitle
            recipeRating.text = "${recipe.avgRating}★"
            recipeDescription.text = "${recipe.difficultyLevel} • ${recipe.cookingTime} mins\n${recipe.cuisineType}"
            recipeImage.setImageResource(recipe.imageResId)

            // Click listener handling in RecyclerView items based on Android developer guide
            // https://developer.android.com/guide/topics/ui/controls/button
            itemView.setOnClickListener {
                onClick(recipe)
            }
        }
    }

    // RecyclerView data update and adapter notification based on Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifydatasetchanged
    fun updateRecipes(newRecipes: List<RecipeCardModel>) {
        recipeList = newRecipes
        notifyDataSetChanged()
    }
}

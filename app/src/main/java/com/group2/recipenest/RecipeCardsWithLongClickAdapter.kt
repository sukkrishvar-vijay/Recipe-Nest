package com.group2.recipenest

import RecipeCardModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView



class RecipeCardsWithLongClickAdapter(
    private var recipeList: List<RecipeCardModel>,
    private val onClick: (RecipeCardModel) -> Unit,
    private val onLongClick: (RecipeCardModel) -> Unit
) : RecyclerView.Adapter<RecipeCardsWithLongClickAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.bind(recipe, onClick, onLongClick)
    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        private val recipeImage: ImageView = itemView.findViewById(R.id.image_container)
        private val recipeRating: TextView = itemView.findViewById(R.id.recipe_rating)

        fun bind(
            recipe: RecipeCardModel,
            onClick: (RecipeCardModel) -> Unit,
            onLongClick: (RecipeCardModel) -> Unit
        ) {
            recipeTitle.text = recipe.recipeTitle
            recipeRating.text = "${recipe.avgRating}★"
            recipeDescription.text = "${recipe.difficultyLevel} • ${recipe.cookingTime} mins\n${recipe.cuisineType}"

            Glide.with(itemView.context)
                .load(recipe.recipeImageUrl)
                .placeholder(R.drawable.placeholder_recipe_image)
                .into(recipeImage)

            itemView.setOnClickListener { onClick(recipe) }
            itemView.setOnLongClickListener {
                itemView.isSelected = true
                onLongClick(recipe)
                true
            }
        }
    }

    fun updateRecipes(newRecipes: List<RecipeCardModel>) {
        recipeList = newRecipes
        notifyDataSetChanged()
    }
}

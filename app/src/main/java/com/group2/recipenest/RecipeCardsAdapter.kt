package com.group2.recipenest
import RecipeCardModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

class RecipeCardsAdapter (private val recipeList: List<RecipeCardModel>) : RecyclerView.Adapter<RecipeCardsAdapter.RecipeViewHolder>() {
    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        val recipeImage: ImageView = itemView.findViewById(R.id.image_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.recipeTitle.text = recipe.title
        holder.recipeDescription.text = recipe.description
        holder.recipeImage.setImageResource(recipe.imageResId)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }
}
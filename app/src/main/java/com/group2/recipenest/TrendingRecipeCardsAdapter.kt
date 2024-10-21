import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

class TrendingRecipeCardsAdapter(
    private var recipes: List<TrendingRecipeCardsModel>,
    private val onClick: (TrendingRecipeCardsModel) -> Unit
) : RecyclerView.Adapter<TrendingRecipeCardsAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeImageView.setImageResource(recipe.imageResId)
        holder.recipeTitleTextView.text = recipe.recipeTitle
        holder.recipeDetailsTextView.text = "${recipe.difficultyLevel} • ${recipe.cookingTime} mins • ${recipe.cuisineType}"
        holder.recipeRatingTextView.text = "${recipe.avgRating}★"

        // Set click listener for the card
        holder.itemView.setOnClickListener {
            onClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    // Function to update the recipes list and notify the adapter
    fun updateTrendingRecipes(newRecipes: List<TrendingRecipeCardsModel>) {
        recipes = newRecipes
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        val recipeTitleTextView: TextView = itemView.findViewById(R.id.recipeTitleTextView)
        val recipeDetailsTextView: TextView = itemView.findViewById(R.id.recipeDetailsTextView)
        val recipeRatingTextView: TextView = itemView.findViewById(R.id.recipeRatingTextView)
    }
}

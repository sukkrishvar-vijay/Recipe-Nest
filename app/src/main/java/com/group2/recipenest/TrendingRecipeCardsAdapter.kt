import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

class TrendingRecipeCardsAdapter(private val recipes: List<TrendingRecipeCardsModel>) :
    RecyclerView.Adapter<TrendingRecipeCardsAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeImageView.setImageResource(recipe.imageId)
        holder.recipeTitleTextView.text = recipe.name
        holder.recipeDetailsTextView.text = recipe.details
        holder.recipeRatingTextView.text = recipe.rating

    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        val recipeTitleTextView: TextView = itemView.findViewById(R.id.recipeTitleTextView)
        val recipeDetailsTextView: TextView = itemView.findViewById(R.id.recipeDetailsTextView)
        val recipeRatingTextView: TextView = itemView.findViewById(R.id.recipeRatingTextView)
    }
}

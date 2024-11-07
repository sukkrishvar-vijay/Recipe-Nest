/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipesCarouselModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView

// RecyclerView Adapter implementation based on Android developer documentation
// https://developer.android.com/guide/topics/ui/layout/recyclerview
class RecipesCarouselAdapter(
    private var carouselItems: List<RecipesCarouselModel>,
    private val onClick: (RecipesCarouselModel) -> Unit
) : RecyclerView.Adapter<RecipesCarouselAdapter.CarouselViewHolder>() {

    // ViewHolder creation and view inflation based on Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder
    // https://developer.android.com/reference/kotlin/android/view/LayoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = carouselItems[position]
        holder.bind(item, onClick)
    }

    override fun getItemCount(): Int = carouselItems.size

    // Updating RecyclerView data and notifying adapter adapted from Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifydatasetchanged
    fun updateCarouselItems(newItems: List<RecipesCarouselModel>) {
        carouselItems = newItems
        notifyDataSetChanged()
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.carousel_image_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.carousel_recipe_title)

        // Data binding in RecyclerView ViewHolder adapted from Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview#bind-data
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/
        fun bind(item: RecipesCarouselModel, onClick: (RecipesCarouselModel) -> Unit) {
            // Use Glide to load the image URL if provided, or use a placeholder image
            Glide.with(itemView.context)
                .load(item.recipeImageUrl)  // Use item.imageUrl if it’s a URL, or item.imageResId for local resources
                .placeholder(R.drawable.placeholder_recipe_image) // Placeholder while loading
                .into(imageView)
            titleTextView.text = item.recipeTitle

            // Handling item click events in RecyclerView ViewHolder based on Android developer guide
            // https://developer.android.com/guide/topics/ui/controls/button
            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}
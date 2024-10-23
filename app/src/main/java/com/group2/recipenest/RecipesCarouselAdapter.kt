package com.group2.recipenest

import RecipesCarouselModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipesCarouselAdapter(
    private val carouselItems: List<RecipesCarouselModel>,
    private val onClick: (RecipesCarouselModel) -> Unit // Callback for handling clicks on each item
) : RecyclerView.Adapter<RecipesCarouselAdapter.CarouselViewHolder>() {

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

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitleTextView)

        // Bind the data to the views
        fun bind(item: RecipesCarouselModel, onClick: (RecipesCarouselModel) -> Unit) {
            titleTextView.text = item.recipeTitle
            imageView.setImageResource(item.imageResId) // Assuming a drawable resource is provided for image

            // Handle click event for the carousel item
            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}

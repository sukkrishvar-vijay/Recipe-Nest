package com.group2.recipenest

import RecipesCarouselModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipesCarouselAdapter(
    private var carouselItems: List<RecipesCarouselModel>,
    private val onClick: (RecipesCarouselModel) -> Unit  // This callback will handle the click event
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

    fun updateCarouselItems(newItems: List<RecipesCarouselModel>) {
        carouselItems = newItems
        notifyDataSetChanged()
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.carousel_image_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.carousel_recipe_title)

        fun bind(item: RecipesCarouselModel, onClick: (RecipesCarouselModel) -> Unit) {
            imageView.setImageResource(item.imageResId)  // Set the image
            titleTextView.text = item.recipeTitle  // Set the title

            // Set up click listener to handle item clicks
            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}
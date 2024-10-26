/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import FavoriteCollectionsTileModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

// RecyclerView adapter implementation based on Android developer documentation
// https://developer.android.com/guide/topics/ui/layout/recyclerview

class FavoritesTileAdapter(private var tileList: List<FavoriteCollectionsTileModel>, private val onClick: (FavoriteCollectionsTileModel) -> Unit) :
    RecyclerView.Adapter<FavoritesTileAdapter.TileViewHolder>() {

    // Data update handling in RecyclerView adapted from Android documentation
    // https://developer.android.com/guide/topics/ui/layout/recyclerview
   //  https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/
    fun updateTiles(newTileList: List<FavoriteCollectionsTileModel>) {
        tileList = newTileList
        notifyDataSetChanged()
    }

    // ViewHolder pattern and View inflation in RecyclerView learned from Android developer guide
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder
    // https://developer.android.com/reference/android/view/LayoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_tile_item, parent, false)
        return TileViewHolder(view)
    }

    // ViewHolder data binding in RecyclerView adapted from Android developer documentation
    // https://developer.android.com/guide/topics/ui/layout/recyclerview#java
    // https://developer.mescius.com/componentone/docs/services/online-datacollection/C1.Android.DataCollection~C1.Android.DataCollection.C1RecyclerViewAdapter%601~OnBindViewHolder.html
    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        val tile = tileList[position]
        holder.bind(tile, onClick)
    }

    override fun getItemCount(): Int = tileList.size

    class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tileTitle: TextView = itemView.findViewById(R.id.list_item_title)
        private val trailingIcon: ImageView = itemView.findViewById(R.id.list_item_trailing_icon)
        private val tileLayout: ConstraintLayout = itemView.findViewById(R.id.tile_layout)

        fun bind(tile: FavoriteCollectionsTileModel, onClick: (FavoriteCollectionsTileModel) -> Unit) {

            // Customizing UI elements in RecyclerView ViewHolder based on Android developer documentation
            // https://developer.android.com/reference/android/widget/TextView
            // https://learn.microsoft.com/en-us/dotnet/api/android.widget.imageview.setimageresource?view=net-android-34.0
            tileTitle.text = "${tile.title} (${tile.count})"
            trailingIcon.setImageResource(R.drawable.ic_right)

            // Click event handling for RecyclerView items adapted from Android documentation
            // https://developer.android.com/guide/topics/ui/layout/recyclerview#java
            tileLayout.setOnClickListener { onClick(tile) }
        }
    }
}

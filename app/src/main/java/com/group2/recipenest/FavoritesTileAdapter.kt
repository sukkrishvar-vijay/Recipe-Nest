package com.group2.recipenest

import FavoriteCollectionsTileModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class FavoritesTileAdapter(private val tileList: List<FavoriteCollectionsTileModel>, private val onClick: (FavoriteCollectionsTileModel) -> Unit) :
    RecyclerView.Adapter<FavoritesTileAdapter.TileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_tile_item, parent, false)  // Custom layout for each tile
        return TileViewHolder(view)
    }

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
            tileTitle.text = "${tile.title} (${tile.count})"
            trailingIcon.setImageResource(R.drawable.ic_right)
            tileLayout.setOnClickListener { onClick(tile) }
        }
    }
}


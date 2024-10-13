package com.group2.recipenest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Favorites"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Find the button and set an OnClickListener
        val openRecipeCardsButton = rootView.findViewById<ConstraintLayout>(R.id.favorite_collection_tile)
        openRecipeCardsButton.setOnClickListener {
            // Create an intent to navigate to RecipeCardsListActivity
            val intent = Intent(requireActivity(), RecipeCardsListActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }
}

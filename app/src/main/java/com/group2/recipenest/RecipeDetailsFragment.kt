package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class RecipeDetailsFragment : Fragment() {

    private lateinit var recipeTitleTextView: TextView
    private lateinit var avgRatingTextView: TextView
    private lateinit var shortDetailTextView: TextView
    private lateinit var longDetailTextView: TextView
    private lateinit var recipeOwnerTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.recipe_detail, container, false)

        // Initialize the views
        recipeOwnerTextView = rootView.findViewById(R.id.ownerNameAndUsername)
        avgRatingTextView = rootView.findViewById(R.id.ratingText)
        shortDetailTextView = rootView.findViewById(R.id.selectedFilters)
        longDetailTextView = rootView.findViewById(R.id.aboutRecipeDetails)

        // Set data passed from the arguments
        setRecipeDetails()

        return rootView
    }

    // Function to set recipe details in the views
    private fun setRecipeDetails() {
        // Get the arguments passed from the previous fragment
        val recipeTitle = arguments?.getString("recipeTitle") ?: "Recipe Details"
        val recipeOwner = arguments?.getString("recipeUserId") ?: "Unknown"
        val recipeDescription = arguments?.getString("recipeDescription") ?: "No description available"
        val avgRating = arguments?.getString("avgRating") ?: "N/A"
        val difficultyLevel = arguments?.getString("difficultyLevel") ?: "Unknown"
        val cookingTime = arguments?.getInt("cookingTime") ?: 0
        val cuisineType = arguments?.getString("cuisineType") ?: "Unknown"

        // Set the values in the views
        recipeOwnerTextView.text = recipeOwner
        shortDetailTextView.text = "$difficultyLevel • $cookingTime mins • $cuisineType"
        avgRatingTextView.text = "$avgRating★"
        longDetailTextView.text = recipeDescription

        // Update the toolbar title with the recipe title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = recipeTitle
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))
    }

    override fun onResume() {
        super.onResume()
        // Ensure the toolbar title is updated every time the fragment is resumed
        setRecipeDetails()
    }
}

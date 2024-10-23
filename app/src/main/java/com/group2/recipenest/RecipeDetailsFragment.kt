package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecipeDetailsFragment : Fragment() {

    private lateinit var avgRatingTextView: TextView
    private lateinit var shortDetailTextView: TextView
    private lateinit var longDetailTextView: TextView
    private lateinit var recipeOwnerTextView: TextView
    private lateinit var ratingsCommentsButton: Button
    private lateinit var favoriteButton: ImageButton
    private lateinit var currentRecipeId: String
    private lateinit var firestore: FirebaseFirestore
    private var isFavorite = false
    private var currentFavoriteCategory: String? = null  // Track the current favorite category

    // User ID to be used for checking the favorite status
    private val currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.recipe_detail, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Initialize the views
        recipeOwnerTextView = rootView.findViewById(R.id.ownerNameAndUsername)
        avgRatingTextView = rootView.findViewById(R.id.ratingText)
        shortDetailTextView = rootView.findViewById(R.id.selectedFilters)
        longDetailTextView = rootView.findViewById(R.id.aboutRecipeDetails)
        ratingsCommentsButton = rootView.findViewById(R.id.ratingsCommentsButton)
        favoriteButton = rootView.findViewById(R.id.favoriteButton)

        // Set recipe details from the arguments
        setRecipeDetails()

        // Set up the toolbar with a back button
        setUpToolbarWithBackButton()

        // Floating action button for adding a comment
        val fabWriteComment: FloatingActionButton = rootView.findViewById(R.id.fab_write_comment)
        fabWriteComment.setOnClickListener {
            navigateToPostCommentFragment()
        }

        // Fetch comments, avgRating and update the button text
        fetchAndSetCommentsAndAvgRating()

        // Check if the recipe is already a favorite
        checkIfFavorite(currentRecipeId)

        // Handle favorite button click
        favoriteButton.setOnClickListener {
            showFavoriteDialog(currentRecipeId)
        }

        // Set the click listener for the Ratings and Comments button
        ratingsCommentsButton.setOnClickListener {
            openReviewFragment()
        }

        return rootView
    }

    private fun setRecipeDetails() {
        val arguments = arguments ?: return

        val recipeTitle = arguments.getString("recipeTitle") ?: "Recipe Details"
        val recipeOwner = arguments.getString("recipeUserId") ?: "Unknown"
        val recipeDescription = arguments.getString("recipeDescription") ?: "No description available"
        val difficultyLevel = arguments.getString("difficultyLevel") ?: "Unknown"
        val cookingTime = arguments.getInt("cookingTime", 0)
        val cuisineType = arguments.getString("cuisineType") ?: "Unknown"
        currentRecipeId = arguments.getString("recipeId") ?: "Unknown"

        // Fetch and set recipe owner details
        fetchAndSetRecipeOwnerDetails(recipeOwner)

        // Set other recipe details
        shortDetailTextView.text = "$difficultyLevel • $cookingTime mins • $cuisineType"
        longDetailTextView.text = recipeDescription

        // Update the toolbar title with the recipe title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = recipeTitle
    }

    private fun fetchAndSetRecipeOwnerDetails(recipeUserId: String) {
        val userRef = firestore.collection("User").document(recipeUserId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val username = document.getString("username") ?: ""
                recipeOwnerTextView.text = "$firstName $lastName • $username"
            } else {
                recipeOwnerTextView.text = "Unknown User"
            }
        }.addOnFailureListener {
            recipeOwnerTextView.text = "Error loading user"
        }
    }

    private fun fetchAndSetCommentsAndAvgRating() {
        val recipeRef = firestore.collection("Recipes").document(currentRecipeId)
        recipeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val comments = document.get("comments") as? List<Map<String, Any>>
                val commentCount = comments?.size ?: 0
                ratingsCommentsButton.text = "Ratings and Comments ($commentCount)"

                val avgRating = document.getDouble("avgRating") ?: 0.0
                avgRatingTextView.text = "${avgRating}★"
            } else {
                ratingsCommentsButton.text = "Ratings and Comments (0)"
                avgRatingTextView.text = "N/A★"
            }
        }.addOnFailureListener {
            ratingsCommentsButton.text = "Ratings and Comments (0)"
            avgRatingTextView.text = "N/A★"
        }
    }

    private fun checkIfFavorite(recipeId: String) {
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favorites = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                favorites?.forEach { categoryMap ->
                    categoryMap.forEach { (category, recipeIds) ->
                        if (recipeIds.contains(recipeId)) {
                            isFavorite = true
                            currentFavoriteCategory = category  // Set current favorite category
                        }
                    }
                }
                updateFavoriteIcon(isFavorite)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to check favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpToolbarWithBackButton() {
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow) // Use your custom back icon
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed() // Navigate back
        }
    }

    private fun showFavoriteDialog(recipeId: String) {
        val userRef = firestore.collection("User").document(currentUserId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val favoriteCategories = favoriteCollection?.map { it.keys.first() } ?: emptyList()

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select Favorite Category")

                val options = favoriteCategories.toTypedArray()
                var selectedOption: String? = currentFavoriteCategory

                builder.setSingleChoiceItems(options, options.indexOf(currentFavoriteCategory)) { _, which ->
                    selectedOption = options[which]
                }

                builder.setPositiveButton("Save") { _, _ ->
                    if (!selectedOption.isNullOrEmpty()) {
                        if (currentFavoriteCategory != null && currentFavoriteCategory != selectedOption) {
                            // Remove from current favorite category and add to the new one
                            removeRecipeFromCurrentCategory(recipeId, currentFavoriteCategory!!)
                            addRecipeToFavoriteCategory(recipeId, selectedOption!!)
                        } else if (currentFavoriteCategory == null) {
                            // If not already in favorites, add to the selected category
                            addRecipeToFavoriteCategory(recipeId, selectedOption!!)
                        }
                    }
                }

                builder.setNegativeButton("Cancel", null)
                builder.create().show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load favorite categories", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addRecipeToFavoriteCategory(recipeId: String, category: String) {
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val updatedFavorites = favoriteCollection?.map {
                    if (it.containsKey(category)) {
                        it.toMutableMap().apply {
                            this[category] = this[category]!!.plus(recipeId)  // Add the recipeId to the correct category
                        }
                    } else {
                        it
                    }
                }?.toList()

                firestore.collection("User").document(currentUserId)
                    .update("favoriteCollection", updatedFavorites)
                    .addOnSuccessListener {
                        isFavorite = true
                        currentFavoriteCategory = category
                        updateFavoriteIcon(isFavorite)
                        Toast.makeText(requireContext(), "Added to $category", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add favorite", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to retrieve favorite categories", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeRecipeFromCurrentCategory(recipeId: String, category: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                if (favoriteCollection != null) {
                    val updatedFavorites = favoriteCollection.map { categoryMap ->
                        if (categoryMap.containsKey(category)) {
                            // Remove the recipeId from the list of recipes in the current category
                            categoryMap.toMutableMap().apply {
                                this[category] = this[category]!!.filterNot { it == recipeId }
                            }
                        } else {
                            categoryMap // Return the map as is if the category doesn't match
                        }
                    }

                    // Update the favoriteCollection in Firestore
                    userRef.update("favoriteCollection", updatedFavorites)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Removed from $category", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to retrieve favorite categories", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to open the ReviewFragment when Ratings and Comments button is clicked
    private fun openReviewFragment() {
        val reviewFragment = ReviewFragment() // Create an instance of your ReviewFragment

        // Pass any necessary arguments, e.g., the recipeId
        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        reviewFragment.arguments = bundle

        // Show as a bottom sheet dialog
        reviewFragment.show(requireActivity().supportFragmentManager, reviewFragment.tag)
    }


    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_outline)
        }
    }

    override fun onResume() {
        super.onResume()
        setRecipeDetails()
        fetchAndSetCommentsAndAvgRating()
    }

    private fun navigateToPostCommentFragment() {
        val postCommentFragment = PostCommentFragment()
        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        postCommentFragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, postCommentFragment)
            .addToBackStack(null)
            .commit()
    }
}

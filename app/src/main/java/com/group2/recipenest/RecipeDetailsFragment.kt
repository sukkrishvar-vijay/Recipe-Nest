/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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
    private var currentFavoriteCategory: String? = null

    private val currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.recipe_detail, container, false)

        firestore = Firebase.firestore

        recipeOwnerTextView = rootView.findViewById(R.id.ownerNameAndUsername)
        avgRatingTextView = rootView.findViewById(R.id.ratingText)
        shortDetailTextView = rootView.findViewById(R.id.selectedFilters)
        longDetailTextView = rootView.findViewById(R.id.aboutRecipeDetails)
        ratingsCommentsButton = rootView.findViewById(R.id.ratingsCommentsButton)
        favoriteButton = rootView.findViewById(R.id.favoriteButton)

        setRecipeDetails()

        setUpToolbarWithBackButton()

        // FloatingActionButton usage and event handling based on Android developer guide
        // https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton
        val fabWriteComment: FloatingActionButton = rootView.findViewById(R.id.fab_write_comment)
        fabWriteComment.setOnClickListener {
            navigateToPostCommentFragment()
        }

        fetchAndSetCommentsAndAvgRating()

        checkIfFavorite(currentRecipeId)

        favoriteButton.setOnClickListener {
            showFavoriteDialog(currentRecipeId)
        }

        ratingsCommentsButton.setOnClickListener {
            openReviewFragment()
        }

        return rootView
    }

    // Retrieving fragment arguments using Bundle and updating UI based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    private fun setRecipeDetails() {
        val arguments = arguments ?: return

        val recipeTitle = arguments.getString("recipeTitle") ?: "Recipe Details"
        val recipeOwner = arguments.getString("recipeUserId") ?: "Unknown"
        val recipeDescription = arguments.getString("recipeDescription") ?: "No description available"
        val difficultyLevel = arguments.getString("difficultyLevel") ?: "Unknown"
        val cookingTime = arguments.getInt("cookingTime", 0)
        val cuisineType = arguments.getString("cuisineType") ?: "Unknown"
        currentRecipeId = arguments.getString("recipeId") ?: "Unknown"

        fetchAndSetRecipeOwnerDetails(recipeOwner)

        shortDetailTextView.text = "$difficultyLevel • $cookingTime mins • $cuisineType"
        longDetailTextView.text = recipeDescription

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
        // Firestore document retrieval and querying based on Firebase documentation
        // https://firebase.google.com/docs/firestore/query-data/get-data
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

    // Firestore document retrieval and updates based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    private fun checkIfFavorite(recipeId: String) {
        // Firestore document retrieval and updates based on Firebase documentation
        // https://firebase.google.com/docs/firestore/query-data/get-data
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favorites = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                favorites?.forEach { categoryMap ->
                    categoryMap.forEach { (category, recipeIds) ->
                        if (recipeIds.contains(recipeId)) {
                            isFavorite = true
                            currentFavoriteCategory = category
                        }
                    }
                }
                updateFavoriteIcon(isFavorite)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to check favorites", Toast.LENGTH_SHORT).show()
        }
    }

    // Toolbar navigation setup based on Android developer guide
    // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
    private fun setUpToolbarWithBackButton() {
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showFavoriteDialog(recipeId: String) {
        // Firestore document retrieval and updates based on Firebase documentation
        // https://firebase.google.com/docs/firestore/query-data/get-data
        val userRef = firestore.collection("User").document(currentUserId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val favoriteCategories = favoriteCollection?.map { it.keys.first() } ?: emptyList()

                // AlertDialog creation and handling based on Android developer documentation
                // https://developer.android.com/reference/androidx/appcompat/app/AlertDialog
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
                            removeRecipeFromCurrentCategory(recipeId, currentFavoriteCategory!!)
                            addRecipeToFavoriteCategory(recipeId, selectedOption!!)
                        } else if (currentFavoriteCategory == null) {
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

    // Firestore document retrieval, array modification, and update logic based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
    private fun addRecipeToFavoriteCategory(recipeId: String, category: String) {
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val updatedFavorites = favoriteCollection?.map {
                    if (it.containsKey(category)) {
                        it.toMutableMap().apply {
                            this[category] = this[category]!!.plus(recipeId)
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

    // Firestore document retrieval and update logic based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
    private fun removeRecipeFromCurrentCategory(recipeId: String, category: String) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                if (favoriteCollection != null) {
                    val updatedFavorites = favoriteCollection.map { categoryMap ->
                        if (categoryMap.containsKey(category)) {
                            categoryMap.toMutableMap().apply {
                                this[category] = this[category]!!.filterNot { it == recipeId }
                            }
                        } else {
                            categoryMap
                        }
                    }

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

    // Opening fragment with arguments using Bundle and showing fragment as a dialog
    // based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    private fun openReviewFragment() {
        val reviewFragment = ReviewFragment()

        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        reviewFragment.arguments = bundle

        reviewFragment.show(requireActivity().supportFragmentManager, reviewFragment.tag)
    }


    // Dynamically updating UI components (ImageButton) based on conditions using setImageResource()
    // based on Android developer documentation
    // https://developer.android.com/reference/android/widget/ImageButton#setImageResource(int)
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

        // Passing data between fragments using Bundle based on Android developer documentation
        // https://developer.android.com/guide/fragments/communicate
        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        postCommentFragment.arguments = bundle

        // Fragment navigation and transaction pattern adapted from Android developer documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, postCommentFragment)
            .addToBackStack(null)
            .commit()
    }
}

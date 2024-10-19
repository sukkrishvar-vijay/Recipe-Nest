package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
    private lateinit var ratingsCommentsButton: Button  // Now declared as a Button
    private lateinit var currentRecipeId: String  // Store the recipeId
    private lateinit var firestore: FirebaseFirestore  // Firestore instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.recipe_detail, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Initialize the views
        recipeOwnerTextView = rootView.findViewById(R.id.ownerNameAndUsername)
        avgRatingTextView = rootView.findViewById(R.id.ratingText)
        shortDetailTextView = rootView.findViewById(R.id.selectedFilters)
        longDetailTextView = rootView.findViewById(R.id.aboutRecipeDetails)
        ratingsCommentsButton = rootView.findViewById(R.id.ratingsCommentsButton)  // Initialize as Button

        // Set data passed from the arguments
        setRecipeDetails()

        // Set up the floating action button for adding a comment
        val fabWriteComment: FloatingActionButton = rootView.findViewById(R.id.fab_write_comment)
        fabWriteComment.setOnClickListener {
            navigateToPostCommentFragment()
        }

        // Fetch comments, avgRating and update the button text
        fetchAndSetCommentsAndAvgRating()

        return rootView
    }

    // Function to set recipe details in the views
    private fun setRecipeDetails() {
        // Ensure arguments are available before trying to retrieve them
        val arguments = arguments ?: return

        // Get the arguments passed from the previous fragment
        val recipeTitle = arguments.getString("recipeTitle") ?: "Recipe Details"
        val recipeOwner = arguments.getString("recipeUserId") ?: "Unknown"
        val recipeDescription = arguments.getString("recipeDescription") ?: "No description available"
        val difficultyLevel = arguments.getString("difficultyLevel") ?: "Unknown"
        val cookingTime = arguments.getInt("cookingTime", 0)
        val cuisineType = arguments.getString("cuisineType") ?: "Unknown"
        currentRecipeId = arguments.getString("recipeId") ?: "Unknown"  // Retrieve the recipeId

        // Fetch and set the recipe owner's details
        fetchAndSetRecipeOwnerDetails(recipeOwner)

        // Set other recipe details
        shortDetailTextView.text = "$difficultyLevel • $cookingTime mins • $cuisineType"
        longDetailTextView.text = recipeDescription

        // Update the toolbar title with the recipe title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = recipeTitle
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))
    }

    // Function to fetch recipe owner details from Firestore and set them in the TextView
    private fun fetchAndSetRecipeOwnerDetails(recipeUserId: String) {
        // Reference to the User document
        val userRef = firestore.collection("User").document(recipeUserId)

        // Fetch the user data
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Extract user details
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val username = document.getString("username") ?: ""

                // Set the formatted text in the TextView
                recipeOwnerTextView.text = "$firstName $lastName • $username"
            } else {
                // Handle the case where the document doesn't exist
                recipeOwnerTextView.text = "Unknown User"
            }
        }.addOnFailureListener {
            // Handle any errors during the fetch
            recipeOwnerTextView.text = "Error loading user"
        }
    }

    // Function to fetch comments, avgRating and update the button text and ratingText
    private fun fetchAndSetCommentsAndAvgRating() {
        // Reference to the recipe document
        val recipeRef = firestore.collection("Recipes").document(currentRecipeId)

        // Fetch the recipe details
        recipeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Fetch the comments array
                val comments = document.get("comments") as? List<Map<String, Any>>
                val commentCount = comments?.size ?: 0

                // Update the button text with the number of comments
                ratingsCommentsButton.text = "Ratings and Comments ($commentCount)"

                // Fetch and update avgRating
                val avgRating = document.getDouble("avgRating") ?: 0.0
                avgRatingTextView.text = String.format("%.1f★", avgRating)  // Display avgRating with 1 decimal place
            } else {
                // Handle the case where the document doesn't exist
                ratingsCommentsButton.text = "Ratings and Comments (0)"
                avgRatingTextView.text = "N/A★"
            }
        }.addOnFailureListener {
            // Handle any errors during the fetch
            ratingsCommentsButton.text = "Ratings and Comments (0)"
            avgRatingTextView.text = "N/A★"
        }
    }

    // Navigate to PostCommentFragment and pass the recipeId
    private fun navigateToPostCommentFragment() {
        val postCommentFragment = PostCommentFragment()

        // Pass the recipeId to PostCommentFragment using a bundle
        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)  // Pass the recipeId
        postCommentFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, postCommentFragment)
            .addToBackStack(null)  // This allows the user to navigate back to this fragment
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Ensure the toolbar title is updated every time the fragment is resumed
        setRecipeDetails()

        // Fetch and update comments and avgRating when the fragment is resumed
        fetchAndSetCommentsAndAvgRating()
    }
}

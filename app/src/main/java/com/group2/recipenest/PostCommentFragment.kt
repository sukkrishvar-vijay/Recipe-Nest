package com.group2.recipenest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class PostCommentFragment : Fragment() {

    private val currentUserId = "ceZ4r5FauC7TuTyckeRp"  // Current user ID
    private lateinit var recipeId: String  // To hold the passed recipeId
    private lateinit var firestore: FirebaseFirestore  // Firestore instance

    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView

    private var currentRating = 0  // To hold the current rating selected by the user

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_post_comment, container, false)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Retrieve the recipeId passed from RecipeDetailsFragment
        recipeId = arguments?.getString("recipeId") ?: ""
        Log.d("PostCommentFragment", "Received Recipe ID: $recipeId")

        // Ensure recipeId is valid
        if (recipeId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid recipe ID", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()  // Navigate back
            return null
        }

        // Initialize the views for stars
        star1 = rootView.findViewById(R.id.star1)
        star2 = rootView.findViewById(R.id.star2)
        star3 = rootView.findViewById(R.id.star3)
        star4 = rootView.findViewById(R.id.star4)
        star5 = rootView.findViewById(R.id.star5)

        // Set up click listeners for stars
        setupStarClickListeners()

        // Initialize your views (e.g., comment text field, post button, etc.)
        val commentEditText: EditText = rootView.findViewById(R.id.commentText)
        val postButton: Button = rootView.findViewById(R.id.postButton)

        // Set up the post button to handle submitting the comment
        postButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            val rating = getDynamicRating()  // Get the rating based on selected stars

            Log.d("PostCommentFragment", "Attempting to post comment: $comment with rating: $rating")

            if (comment.isNotEmpty()) {
                postComment(comment, recipeId, rating)  // Post the comment with the recipeId and rating
            } else {
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    // Function to set up click listeners for the stars
    private fun setupStarClickListeners() {
        val stars = listOf(star1, star2, star3, star4, star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStarRating(index + 1)
            }
        }
    }

    // Function to update the stars' appearance based on the rating
    private fun updateStarRating(rating: Int) {
        currentRating = rating
        val stars = listOf(star1, star2, star3, star4, star5)
        for (i in stars.indices) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled)  // Change to filled star
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline)  // Change to outline star
            }
        }
    }

    // Function to retrieve dynamic rating based on clicked stars
    private fun getDynamicRating(): Double {
        return currentRating.toDouble()
    }

    // Function to handle posting the comment to Firestore and updating avgRating
    private fun postComment(comment: String, recipeId: String, rating: Double) {
        val recipeRef = firestore.collection("Recipes").document(recipeId)

        // First, get the current recipe details including avgRating and comments
        recipeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentAvgRating = document.getDouble("avgRating") ?: 0.0
                val commentsList = document.get("comments") as? List<Map<String, Any>> ?: emptyList()

                // Calculate the new average rating
                val numberOfRatings = commentsList.size
                var newAvgRating = (currentAvgRating * numberOfRatings + rating) / (numberOfRatings + 1)

                // Round the newAvgRating to one decimal place
                newAvgRating = String.format("%.1f", newAvgRating).toDouble()

                // Prepare the comment details
                val commentData = hashMapOf(
                    "commenter" to currentUserId,
                    "comment" to comment,
                    "dateCommented" to Date(),
                    "rating" to rating
                )

                // Update the comments array and avgRating in a single batch
                firestore.runBatch { batch ->
                    batch.update(recipeRef, "comments", FieldValue.arrayUnion(commentData))
                    batch.update(recipeRef, "avgRating", newAvgRating)
                }.addOnSuccessListener {
                    Log.d("PostCommentFragment", "Comment posted successfully and avgRating updated")
                    Toast.makeText(requireContext(), "Comment added successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()  // Go back to the previous fragment
                }.addOnFailureListener { exception ->
                    Log.e("PostCommentFragment", "Failed to post comment and update avgRating", exception)
                    Toast.makeText(requireContext(), "Failed to add comment: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("PostCommentFragment", "Failed to retrieve recipe", exception)
            Toast.makeText(requireContext(), "Failed to retrieve recipe: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}

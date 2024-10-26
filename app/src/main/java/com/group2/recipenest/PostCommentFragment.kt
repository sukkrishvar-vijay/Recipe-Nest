/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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

    private val currentUserId = userSignInData.UserDocId
    private lateinit var recipeId: String
    private lateinit var firestore: FirebaseFirestore

    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView

    private var currentRating = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_post_comment, container, false)

        firestore = FirebaseFirestore.getInstance()

        // Retrieving fragment arguments using Bundle based on Android developer documentation
        // https://developer.android.com/guide/fragments/communicate
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/is-empty.html
        recipeId = arguments?.getString("recipeId") ?: ""

        if (recipeId.isEmpty()) {
            parentFragmentManager.popBackStack()
            return null
        }

        star1 = rootView.findViewById(R.id.star1)
        star2 = rootView.findViewById(R.id.star2)
        star3 = rootView.findViewById(R.id.star3)
        star4 = rootView.findViewById(R.id.star4)
        star5 = rootView.findViewById(R.id.star5)

        setupStarClickListeners()

        val commentEditText: EditText = rootView.findViewById(R.id.commentText)
        val postButton: Button = rootView.findViewById(R.id.postButton)

        postButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            val rating = getDynamicRating()

            if (comment.isNotEmpty()) {
                postComment(comment, recipeId, rating)
            } else {
                // Toast messages implementation based on Android developer guide
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    // Star rating click listener pattern adapted from Android developer tutorial on custom views
    // https://developer.android.com/guide/topics/ui/custom-components
    // https://discuss.kotlinlang.org/t/trying-to-understand-onclicklistener/24773
    private fun setupStarClickListeners() {
        val stars = listOf(star1, star2, star3, star4, star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStarRating(index + 1)
            }
        }
    }

    // Custom star rating display logic adapted from Android UI tutorials
    // https://developer.android.com/guide/topics/ui/custom-components
    private fun updateStarRating(rating: Int) {
        currentRating = rating
        val stars = listOf(star1, star2, star3, star4, star5)
        for (i in stars.indices) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled)
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline)
            }
        }
    }

    private fun getDynamicRating(): Double {
        return currentRating.toDouble()
    }

    // Firestore document retrieval and batch update learned from Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://firebase.google.com/docs/firestore/manage-data/transactions
    private fun postComment(comment: String, recipeId: String, rating: Double) {
        val recipeRef = firestore.collection("Recipes").document(recipeId)

        recipeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentAvgRating = document.getDouble("avgRating") ?: 0.0
                val commentsList = document.get("comments") as? List<Map<String, Any>> ?: emptyList()

                val numberOfRatings = commentsList.size
                var newAvgRating = (currentAvgRating * numberOfRatings + rating) / (numberOfRatings + 1)

                newAvgRating = String.format("%.1f", newAvgRating).toDouble()

                val commentData = hashMapOf(
                    "commenter" to currentUserId,
                    "comment" to comment,
                    "dateCommented" to Date(),
                    "rating" to rating
                )

                // Firestore batch write with array updates learned from Firebase documentation
                // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
                // https://developer.android.com/guide/fragments/fragmentmanager
                firestore.runBatch { batch ->
                    batch.update(recipeRef, "comments", FieldValue.arrayUnion(commentData))
                    batch.update(recipeRef, "avgRating", newAvgRating)
                }.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Comment added successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to add comment: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to retrieve recipe: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}

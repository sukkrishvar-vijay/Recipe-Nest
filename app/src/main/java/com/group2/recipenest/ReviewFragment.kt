package com.group2.recipenest

import ReviewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ReviewFragment : Fragment() {

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private lateinit var firestore: FirebaseFirestore
    private var currentRecipeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rating_and_comments, container, false)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get the passed recipeId
        currentRecipeId = arguments?.getString("recipeId")

        // Set up RecyclerView
        reviewsRecyclerView = view.findViewById(R.id.ratings_recycler_view)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize with an empty list for the adapter
        adapter = ReviewAdapter(listOf())
        reviewsRecyclerView.adapter = adapter

        // Fetch comments for the current recipe
        fetchComments()

        return view
    }

    private fun fetchComments() {
        currentRecipeId?.let { recipeId ->
            firestore.collection("Recipes").document(recipeId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val comments = document.get("comments") as? List<Map<String, Any>> ?: emptyList()
                        val reviewList = mutableListOf<ReviewModel>()

                        // Loop through each comment and fetch commenter details
                        for (comment in comments) {
                            val commenterId = comment["commenter"] as? String ?: continue
                            val commentText = comment["comment"] as? String ?: ""
                            val rating = (comment["rating"] as? Long)?.toInt() ?: 0
                            val dateCommented = comment["dateCommented"] as? String ?: ""

                            // Fetch commenter details
                            fetchCommenterDetails(commenterId) { firstName, lastName, username ->
                                val fullName = "$firstName $lastName"
                                val review = ReviewModel(fullName, username, commentText, dateCommented, rating)
                                reviewList.add(review)

                                // Update the adapter once all reviews are loaded
                                adapter.updateReviews(reviewList)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch comments: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fetch the commenter's firstName, lastName, and username from the User collection
    private fun fetchCommenterDetails(commenterId: String, callback: (String, String, String) -> Unit) {
        firestore.collection("User").document(commenterId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "Unknown"
                    val username = document.getString("username") ?: "Unknown"
                    callback(firstName, lastName, username)
                } else {
                    callback("Unknown", "User", "")
                }
            }
            .addOnFailureListener { exception ->
                callback("Unknown", "User", "")
                Toast.makeText(requireContext(), "Failed to fetch user details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

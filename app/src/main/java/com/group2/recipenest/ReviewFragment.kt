package com.group2.recipenest

import ReviewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class ReviewFragment : BottomSheetDialogFragment() {

    private lateinit var ratingRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private var reviewList: MutableList<ReviewModel> = mutableListOf()
    private val db: FirebaseFirestore = Firebase.firestore
    private var recipeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rating_and_comments, container, false)

        // Retrieve the recipeId from the arguments
        recipeId = arguments?.getString("recipeId")

        // Initialize RecyclerView
        ratingRecyclerView = view.findViewById(R.id.ratings_recycler_view)
        ratingRecyclerView.layoutManager = LinearLayoutManager(context)

        // Set up Adapter
        reviewAdapter = ReviewAdapter(reviewList)
        ratingRecyclerView.adapter = reviewAdapter

        // Fetch reviews from Firestore based on the recipeId
        fetchComments()

        return view
    }

    private fun fetchComments() {
        recipeId?.let { recipeId ->
            db.collection("Recipes").document(recipeId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val comments = document.get("comments") as? List<Map<String, Any>> ?: emptyList()
                        reviewList.clear()

                        for (comment in comments) {
                            val commenterId = comment["commenter"] as? String ?: continue
                            val commentText = comment["comment"] as? String ?: ""
                            val rating = (comment["rating"] as? Double)?.toInt() ?: 0
                            val dateCommented = (comment["dateCommented"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()

                            fetchCommenterDetails(commenterId) { firstName, lastName, username ->
                                val fullName = "$firstName $lastName"
                                val review = ReviewModel(fullName, username, commentText, dateCommented, rating)
                                reviewList.add(review)
                                reviewAdapter.notifyItemInserted(reviewList.size - 1)
                            }
                        }
                        reviewAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch comments: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchCommenterDetails(commenterId: String, callback: (String, String, String) -> Unit) {
        db.collection("User").document(commenterId)
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

    override fun onStart() {
        super.onStart()

        // Set up the bottom sheet to display initially at half the screen height
        val dialog = dialog as BottomSheetDialog
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.5).toInt()

            // Enable the bottom sheet to expand fully when swiped up
            behavior.isFitToContents = false // Ensures that the sheet can expand to its full height
            behavior.isHideable = true // Allows the sheet to be dismissed when swiped down
        }
    }
}

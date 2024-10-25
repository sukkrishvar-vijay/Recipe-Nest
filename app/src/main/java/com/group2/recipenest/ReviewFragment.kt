/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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

        recipeId = arguments?.getString("recipeId")

        ratingRecyclerView = view.findViewById(R.id.ratings_recycler_view)
        ratingRecyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(reviewList)
        ratingRecyclerView.adapter = reviewAdapter

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

        val dialog = dialog as BottomSheetDialog
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.5).toInt()

            behavior.isFitToContents = false
            behavior.isHideable = true
        }
    }
}

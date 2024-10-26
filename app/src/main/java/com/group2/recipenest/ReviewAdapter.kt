/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import ReviewModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// RecyclerView Adapter and ViewHolder setup based on Android developer documentation
// https://developer.android.com/guide/topics/ui/layout/recyclerview
class ReviewAdapter(
    private var reviewList: List<ReviewModel>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ratings_and_comments_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    // Updating RecyclerView data and notifying adapter based on Android developer documentation
    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifydatasetchanged
    fun updateReviews(newReviewList: List<ReviewModel>) {
        reviewList = newReviewList
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerAndDate: TextView = itemView.findViewById(R.id.user_name_username_date_commented)
        private val commentText: TextView = itemView.findViewById(R.id.review_text)
        private val ratingText: TextView = itemView.findViewById(R.id.rating)

        // Binding data to ViewHolder in RecyclerView based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview#bind-data
        fun bind(review: ReviewModel) {
            val formattedDate = formatDate(review.dateCommented)

            val fullNameAndDate = "${review.fullName} • ${review.username} • $formattedDate"
            reviewerAndDate.text = fullNameAndDate

            commentText.text = review.comment

            ratingText.text = "${review.rating}★"
        }

        // Date formatting using SimpleDateFormat in Kotlin based on Java documentation
        // https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        // https://kotlinlang.org/docs/exceptions.html
        private fun formatDate(date: Date): String {
            return try {
                val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                outputFormat.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }

}

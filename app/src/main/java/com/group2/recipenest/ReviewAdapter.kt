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

    fun updateReviews(newReviewList: List<ReviewModel>) {
        reviewList = newReviewList
        notifyDataSetChanged()  // Notify adapter to refresh the view
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerAndDate: TextView = itemView.findViewById(R.id.user_name_username_date_commented)
        private val commentText: TextView = itemView.findViewById(R.id.review_text)
        private val ratingText: TextView = itemView.findViewById(R.id.rating)

        fun bind(review: ReviewModel) {
            // Format the date to "03 October, 2024"
            val formattedDate = formatDate(review.dateCommented)

            // Combine full name, username, and formatted date into one string
            val fullNameAndDate = "${review.fullName} • ${review.username} • $formattedDate"
            reviewerAndDate.text = fullNameAndDate

            // Display the comment text
            commentText.text = review.comment

            // Format the rating, assuming rating is between 1-5 and display it
            ratingText.text = "${review.rating}★"
        }

        private fun formatDate(date: Date): String {
            return try {
                val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()) // Output format: "03 October, 2024"
                outputFormat.format(date)
            } catch (e: Exception) {
                "" // Return empty string in case of an error
            }
        }
    }

}

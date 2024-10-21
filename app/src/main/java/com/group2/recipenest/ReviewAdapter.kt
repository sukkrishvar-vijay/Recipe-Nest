package com.group2.recipenest

import ReviewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(
    private var reviewList: List<ReviewModel>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ratings_and_comments_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerAndDate: TextView = itemView.findViewById(R.id.user_name_username_date_commented)
        private val reviewComment: TextView = itemView.findViewById(R.id.review_text)
        private val reviewRating: RatingBar = itemView.findViewById(R.id.rating)

        fun bind(review: ReviewModel) {
            reviewerAndDate.text = "${review.fullName} • ${review.username} • ${review.dateCommented}"
            reviewComment.text = review.comment
        }
    }

    // Method to update the list of reviews
    fun updateReviews(newReviews: List<ReviewModel>) {
        reviewList = newReviews
        notifyDataSetChanged()
    }
}

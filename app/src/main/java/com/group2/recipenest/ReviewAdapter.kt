import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

class ReviewAdapter(private val reviewList: List<Reviews>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ratings_and_comments_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userNameId.text = "${review.userName}. @${review.userId}. ${review.date}"
        holder.reviewText.text = review.reviewText
        holder.ratingText.text = "${review.rating} ★"

        // Optionally play the audio review if available
        if (review.audioFilePath != null) {
            holder.audioPlayButton.visibility = View.VISIBLE
            holder.audioPlayText.visibility = View.VISIBLE
            holder.audioPlayButton.setOnClickListener {
                // Handle audio playback using MediaPlayer or another audio library
            }
        } else {
            holder.audioPlayButton.visibility = View.GONE
            holder.audioPlayText.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameId: TextView = itemView.findViewById(R.id.user_name_id)
        val reviewText: TextView = itemView.findViewById(R.id.review_text)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val audioPlayButton: ImageView = itemView.findViewById(R.id.audio_play_button)
        val audioPlayText: TextView = itemView.findViewById(R.id.audio_play_text)
    }
}
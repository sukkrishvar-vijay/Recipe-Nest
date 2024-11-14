package com.group2.recipenest

import ReviewModel
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private var reviewList: List<ReviewModel>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    companion object {
        private var activeMediaPlayer: MediaPlayer? = null
        @SuppressLint("StaticFieldLeak")
        private var activeViewHolder: ReviewViewHolder? = null
    }

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
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerAndDate: TextView = itemView.findViewById(R.id.user_name_username_date_commented)
        private val commentText: TextView = itemView.findViewById(R.id.review_text)
        private val ratingText: TextView = itemView.findViewById(R.id.rating)
        private val audioSection: LinearLayout = itemView.findViewById(R.id.audioSection)
        private val playAudioButton: ImageButton = itemView.findViewById(R.id.playPauseButton)
        private val audioBar: SeekBar = itemView.findViewById(R.id.audioSeekBar)
        private var mediaPlayer: MediaPlayer? = null
        private var isPlaying: Boolean = false

        private lateinit var runnable: Runnable
        private lateinit var handler: Handler

        fun bind(review: ReviewModel) {
            val formattedDate = formatDate(review.dateCommented)
            val fullNameAndDate = "${review.fullName} • ${review.username} • $formattedDate"
            reviewerAndDate.text = fullNameAndDate
            commentText.text = review.comment
            ratingText.text = "${review.rating}★"

            if (review.audioCommentUrl.isNotEmpty()) {
                audioSection.visibility = View.VISIBLE
                setupAudioPlayback(review.audioCommentUrl)
            } else {
                audioSection.visibility = View.GONE
            }

            handler = Handler(Looper.getMainLooper())
            runnable = Runnable{
                //audioBar.progress = mediaPlayer?.currentPosition!!
                mediaPlayer?.let {
                    audioBar.progress = it.currentPosition
                } ?: Log.e("PostCommentFragment", "mediaPlayer is null when accessing currentPosition")
                handler.postDelayed(runnable,0)
            }

            audioBar.setOnSeekBarChangeListener(object  : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if(p2)
                        mediaPlayer!!.seekTo(p1)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) { }

                override fun onStopTrackingTouch(p0: SeekBar?) { }

            })

        }

        private fun setupAudioPlayback(audioUrl: String) {
            playAudioButton.setOnClickListener {
                if (mediaPlayer == null) {
                    initializeMediaPlayer(audioUrl)
                } else {
                    togglePlayback()
                }
            }
        }

        private fun initializeMediaPlayer(audioUrl: String) {
            activeMediaPlayer?.let {
                activeViewHolder?.stopAudioPlayback()
                activeMediaPlayer?.release()
                activeMediaPlayer = null
                activeViewHolder = null
            }
            FirebaseStorage.getInstance().getReferenceFromUrl(audioUrl).downloadUrl
                .addOnSuccessListener { uri ->
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(uri.toString())
                        setOnPreparedListener {
                            playAudioButton.isEnabled = true
                            audioBar.max = mediaPlayer!!.duration
                            togglePlayback()
                        }
                        setOnCompletionListener {
                            playAudioButton.setImageResource(R.drawable.ic_play) // Set icon back to "play"
                            this@ReviewViewHolder.isPlaying = false
                            handler.removeCallbacks(runnable)
                            mediaPlayer?.seekTo(0)
                            audioBar.progress = 0
                            releasePlayer()
                        }
                        prepareAsync()
                    }
                    activeMediaPlayer = mediaPlayer
                    activeViewHolder = this@ReviewViewHolder
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "Failed to load audio", Toast.LENGTH_SHORT).show()
                }
        }

        private fun togglePlayback() {
            mediaPlayer?.let { player ->
                if (isPlaying) {
                    player.pause()
                    playAudioButton.setImageResource(R.drawable.ic_play) // Set icon to "play"
                    handler.removeCallbacks(runnable)
                } else {
                    player.start()
                    playAudioButton.setImageResource(R.drawable.ic_pause) // Set icon to "pause"
                    handler.postDelayed(runnable, 0)
                }
                isPlaying = !isPlaying
            }
        }

        fun stopAudioPlayback() {
            mediaPlayer?.pause()
            mediaPlayer?.seekTo(0)
            playAudioButton.setImageResource(R.drawable.ic_play)
            audioBar.progress = 0
            handler.removeCallbacks(runnable)
            isPlaying = false
            mediaPlayer = null
        }

        fun releasePlayer() {
            playAudioButton.setImageResource(R.drawable.ic_play)
            audioBar.progress = 0
            handler.removeCallbacks(runnable)
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }

        private fun formatDate(date: Date): String {
            return try {
                val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                outputFormat.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }

    fun releaseAllPlayers() {
        activeViewHolder?.stopAudioPlayback()
        activeMediaPlayer?.release()
        activeMediaPlayer = null
        activeViewHolder = null
    }

    override fun onViewRecycled(holder: ReviewViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }
}

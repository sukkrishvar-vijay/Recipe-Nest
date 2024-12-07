/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
 */

package com.group2.recipenest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.UUID

class PostCommentFragment : Fragment() {

    private val currentUserId = userSignInData.UserDocId
    private lateinit var recipeId: String
    private lateinit var firestore: FirebaseFirestore
    private val firebaseStorageRef = Firebase.storage.reference

    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView

    private lateinit var audioRecord: Button
    private lateinit var playpause: ImageButton
    private lateinit var audioBar: SeekBar
    private lateinit var setAudioTime: TextView

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String = ""
    private var isRecording = false
    private var isAudioAvailable = false
    private var audioUrl: String = ""
    private var canStartRecord:Boolean = true

    private var currentRating = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_post_comment, container, false)

        firestore = FirebaseFirestore.getInstance()

        requestPermissions()

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

        val commentTextLayout: TextInputLayout = rootView.findViewById(R.id.commentText)
        val commentEditText: EditText? = commentTextLayout.editText
        val postButton: Button = rootView.findViewById(R.id.postButton)
        setAudioTime = rootView.findViewById(R.id.timeDuration)

        audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/${UUID.randomUUID()}.aac"

        audioRecord = rootView.findViewById(R.id.recordButton)
        playpause = rootView.findViewById(R.id.playPauseButton)
        audioBar = rootView.findViewById(R.id.audioSeekBar)

        playpause.isEnabled = false
        audioBar.isEnabled = false

        //https://developer.android.com/reference/android/os/Handler
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable{
            mediaPlayer?.let {
                audioBar.progress = it.currentPosition
            } ?: Log.e("PostCommentFragment", "mediaPlayer is null when accessing currentPosition")
            handler.postDelayed(runnable,0)
        }

        audioRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (canStartRecord) {
                    canStartRecord = false
                    startRecording()
                } else {
                    canStartRecord = true
                    stopRecording()
                }
            } else {
                Toast.makeText(requireContext(), "Enable Mic permission in the Settings", Toast.LENGTH_SHORT).show()
            }
        }

        playpause.setOnClickListener {
            if (File(audioFilePath).exists()) {
                audioBar.max = mediaPlayer!!.duration
                playPauseRecording()
            } else {
                Toast.makeText(requireContext(), "No audio file found", Toast.LENGTH_SHORT).show()
            }
        }

        //https://www.youtube.com/watch?v=A3ReceYaoJM&list=PLpZQVidZ65jPz-XIHdWi1iCra8TU9h_kU&index=19
        //https://developer.android.com/reference/android/widget/SeekBar
        audioBar.setOnSeekBarChangeListener(object  : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2)
                    mediaPlayer!!.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }

        })

        postButton.setOnClickListener {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying){
                mediaPlayer!!.pause()
            }

            val comment = commentEditText?.text.toString()
            val rating = getDynamicRating()

            if (comment.isNotEmpty() && currentRating != 0) {
                if(isAudioAvailable){
                    uploadAudioToFirebase { audioDownloadUrl ->
                        audioUrl = audioDownloadUrl
                        postComment(comment, recipeId, rating)
                    }
                }
                else{
                    postComment(comment, recipeId, rating)
                }
            } else {
                if(currentRating == 0){
                    // Toast messages implementation based on Android developer guide
                    // https://developer.android.com/guide/topics/ui/notifiers/toasts
                    Toast.makeText(requireContext(), "Please rate the recipe", Toast.LENGTH_SHORT).show()
                }
                if(comment.isEmpty()){
                    commentTextLayout.error = "Comment is required"
                }
                else{
                    commentTextLayout.error = null
                }
            }
        }
        return rootView
    }

    //https://developer.android.com/about/versions/12/deprecations
    //https://developer.android.com/reference/android/media/MediaRecorder
    //https://developer.android.com/training/data-storage
    @Suppress("DEPRECATION")
    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecord.text = "Recording, Tap to Stop"
            setAudioTime.text = "00:00"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //https://developer.android.com/reference/android/annotation/SuppressLint
    //https://developer.android.com/reference/android/media/MediaRecorder
    //https://developer.android.com/training/data-storage
    @SuppressLint("DefaultLocale")
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false

        isAudioAvailable = true
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            setOnCompletionListener {
                playpause.setImageResource(R.drawable.ic_play)
                handler.removeCallbacks(runnable)
                mediaPlayer?.seekTo(0)
                audioBar.progress = 0
            }
        }
        val durationInMillis = mediaPlayer!!.duration
        val minutes = (durationInMillis / 1000) / 60
        val seconds = (durationInMillis / 1000) % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        setAudioTime.text = formattedTime
        audioRecord.text = "Record Again"
        playpause.isEnabled = true
        audioBar.isEnabled = true
    }

    //function to play and pause recording
    //https://www.youtube.com/watch?v=A3ReceYaoJM&list=PLpZQVidZ65jPz-XIHdWi1iCra8TU9h_kU&index=19
    //https://developer.android.com/reference/android/media/MediaPlayer
    //https://developer.android.com/reference/android/os/Handler
    private fun playPauseRecording() {
        // Toggle play/pause
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            playpause.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(runnable)
        } else {
            mediaPlayer!!.start()
            playpause.setImageResource(R.drawable.ic_pause)
            handler.postDelayed(runnable, 0)
        }
    }

    //Method to upload recorded audio to firebase
    //https://firebase.google.com/docs/storage/android/upload-files
    private fun uploadAudioToFirebase(onUploadSuccess: (String) -> Unit) {
        val audioFile = File(audioFilePath)
        val audioUri = Uri.fromFile(audioFile)
        val audioRef = firebaseStorageRef.child("Audio/${audioFile.name}")

        audioRef.putFile(audioUri)
            .addOnSuccessListener {
                audioRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onUploadSuccess(downloadUrl.toString())
                }.addOnFailureListener {
                    Log.d("FIREBASE", "Failed to retrieve download URL")
                }
            }
            .addOnFailureListener {
                Log.d("FIREBASE", "Upload Failed")
            }
    }

    //function to request user for mic permission
    //https://developer.android.com/training/permissions/requesting
    private fun requestPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val micGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
            val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (!micGranted) {
                Toast.makeText(requireContext(), "Enable microphone permission to record audio comment", Toast.LENGTH_LONG).show()
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
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

    private fun getDynamicRating(): Long {
        return currentRating.toLong()
    }

    // Firestore document retrieval and batch update learned from Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://firebase.google.com/docs/firestore/manage-data/transactions
    private fun postComment(comment: String, recipeId: String, rating: Long) {
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
                    "rating" to rating,
                    "audioCommentUrl" to audioUrl
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }

}
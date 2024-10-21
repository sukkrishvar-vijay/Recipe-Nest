package com.group2.recipenest

import ReviewAdapter
import Reviews
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReviewFragment : BottomSheetDialogFragment() {

    private lateinit var ratingRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewList: List<Reviews>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rating_and_comments, container, false)

        // Initialize RecyclerView
        ratingRecyclerView = view.findViewById(R.id.ratings_recycler_view)
        ratingRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Review List (this could be populated from a database or API)
        reviewList = listOf(
            Reviews("Tony Stark", "tonystark", "23 Sep 2024", "I tried this recipe and it’s delicious.", 4.0f),
            Reviews("Bruce Banner", "hulk", "22 Sep 2024", "Love this! Easy to follow and tasty!", 5.0f),

            )

        // Set up Adapter
        reviewAdapter = ReviewAdapter(reviewList)
        ratingRecyclerView.adapter = reviewAdapter

        return view
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
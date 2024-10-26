/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.LandingPage3Binding
import kotlin.math.abs

class LandingPage3: Fragment() {

    private var _binding: LandingPage3Binding? = null
    private val binding get() = _binding!!

    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = LandingPage3Binding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the GestureDetector
        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())

        // Set an OnTouchListener on the root view to detect swipe gestures
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event) // Pass the MotionEvent to the GestureDetector
            true // Return true to indicate the touch event was handled
        }

        binding.getStartedButton.setOnClickListener {
            setFirstLaunchCompleted()
            loadFragment(SignInFragment())
        }

    }

    // Detect swipe gestures
    //https://stackoverflow.com/questions/17390873/onfling-motionevent-e1-null
    //https://www.geeksforgeeks.org/how-to-detect-swipe-direction-in-android/
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Check if e1 is not null
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY)) { // Detect horizontal swipe
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Right swipe detected
                        onSwipeRight()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun onSwipeRight() {
        // Load the previous landing page or do nothing if on the first page
        // Replace with the fragment for LandingPage3
        // For this example, let's assume LandingPage3 is the previous fragment
        loadFragment(LandingPage2())
    }

    private fun setFirstLaunchCompleted() {
        val sharedPreferences = requireContext().getSharedPreferences("RecipeNestPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
    }

    //https://medium.com/@Max_Sir/mastering-android-fragments-managers-transactions-and-best-practices-in-kotlin-af00cb9b44ac
    //https://developer.android.com/guide/fragments/fragmentmanager
    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
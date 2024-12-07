/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
 */

package com.group2.recipenest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.LandingPage2Binding
import kotlin.math.abs

class LandingPage2: Fragment() {
    private var _binding: LandingPage2Binding? = null
    private val binding get() = _binding!!

    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LandingPage2Binding.inflate(inflater, container, false)
        return binding.root
    }

    //https://developer.android.com/reference/android/view/GestureDetector
    //https://developer.android.com/studio/write/lint#accessibility
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
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
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        onSwipeLeft()
                    }
                    else{
                        onSwipeRight()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun onSwipeLeft() {
        loadFragment(LandingPage3())
    }

    private fun onSwipeRight() {
        loadFragment(LandingPage1())
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
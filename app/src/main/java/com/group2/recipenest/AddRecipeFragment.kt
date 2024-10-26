/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class AddRecipeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    private val currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.add_recipe_fragment, container, false)

        // Firebase Firestore initialization and usage adapted from Firebase documentation
        // https://firebase.google.com/docs/firestore
        firestore = Firebase.firestore

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Add New Recipe"
        toolbar.setTitleTextColor(Color.BLACK)

        val uploadImageButton: Button = rootView.findViewById(R.id.upload_image_button)
        val titleEditText: TextInputEditText = rootView.findViewById(R.id.recipe_title)
        val descriptionEditText: TextInputEditText = rootView.findViewById(R.id.recipe_description)
        val cuisineVegetarian: CheckBox = rootView.findViewById(R.id.cuisine_vegetarian)
        val cuisineNonVegetarian: CheckBox = rootView.findViewById(R.id.cuisine_non_vegetarian)
        val cuisineChinese: CheckBox = rootView.findViewById(R.id.cuisine_chinese)
        val cuisineThai: CheckBox = rootView.findViewById(R.id.cuisine_thai)
        val cuisineIndian: CheckBox = rootView.findViewById(R.id.cuisine_indian)
        val cuisineAmerican: CheckBox = rootView.findViewById(R.id.cuisine_american)
        val difficultyToggleGroup: MaterialButtonToggleGroup = rootView.findViewById(R.id.difficulty_toggle_group)
        val easyButton: MaterialButton = rootView.findViewById(R.id.easy_button)
        val mediumButton: MaterialButton = rootView.findViewById(R.id.medium_button)
        val hardButton: MaterialButton = rootView.findViewById(R.id.hard_button)
        val cookingTimeGroup: RadioGroup = rootView.findViewById(R.id.cooking_time_group)
        val saveRecipeButton: Button = rootView.findViewById(R.id.submit_button)

        // Button click listener implementation learned from Android developer tutorial
        // https://developer.android.com/guide/topics/ui/controls/button
        // https://discuss.kotlinlang.org/t/trying-to-understand-onclicklistener/24773
        uploadImageButton.setOnClickListener {
            // Toast message usage based on Android developer documentation
            // https://developer.android.com/guide/topics/ui/notifiers/toasts
            // https://www.geeksforgeeks.org/toasts-android-studio/
            Toast.makeText(activity, "Upload image clicked", Toast.LENGTH_SHORT).show()
        }

        // Code handling MaterialButtonToggleGroup adapted from Android developer documentation
        // https://developer.android.com/reference/com/google/android/material/button/MaterialButtonToggleGroup
        // https://github.com/material-components/material-components-android/issues/1365
        difficultyToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedButton: MaterialButton = rootView.findViewById(checkedId)
                setSelectedButtonState(selectedButton)
                when (selectedButton) {
                    easyButton -> {
                        resetButtonState(mediumButton)
                        resetButtonState(hardButton)
                    }
                    mediumButton -> {
                        resetButtonState(easyButton)
                        resetButtonState(hardButton)
                    }
                    hardButton -> {
                        resetButtonState(easyButton)
                        resetButtonState(mediumButton)
                    }
                }
            }
        }

        // Button click listener implementation learned from Android developer tutorial
        // https://developer.android.com/guide/topics/ui/controls/button
        // https://discuss.kotlinlang.org/t/trying-to-understand-onclicklistener/24773
        saveRecipeButton.setOnClickListener {
            val recipeTitle = titleEditText.text.toString()
            val recipeDescription = descriptionEditText.text.toString()
            val selectedDifficulty = when {
                easyButton.isChecked -> "Easy"
                mediumButton.isChecked -> "Medium"
                hardButton.isChecked -> "Hard"
                else -> "Unknown"
            }

            val selectedCookingTime = when (cookingTimeGroup.checkedRadioButtonId) {
                R.id.time_15 -> 15
                R.id.time_30 -> 30
                R.id.time_45 -> 45
                R.id.time_60 -> 60
                else -> 0
            }

            val cuisineType = mutableListOf<String>()
            if (cuisineVegetarian.isChecked) {
                cuisineType.add("Vegetarian")
            }
            if (cuisineNonVegetarian.isChecked) {
                cuisineType.add("Non-Vegetarian")
            }
            if (cuisineChinese.isChecked) {
                cuisineType.add("Chinese")
            }
            if (cuisineThai.isChecked) {
                cuisineType.add("Thai")
            }
            if (cuisineIndian.isChecked) {
                cuisineType.add("Indian")
            }
            if (cuisineAmerican.isChecked) {
                cuisineType.add("American")
            }

            if (recipeTitle.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a recipe title", Toast.LENGTH_SHORT).show()
            } else if (selectedCookingTime == 0) {
                Toast.makeText(requireContext(), "Please select a valid cooking time", Toast.LENGTH_SHORT).show()
            } else {
                storeRecipeInFirestore(recipeTitle, recipeDescription, selectedCookingTime, selectedDifficulty, cuisineType)
            }
        }

        return rootView
    }

    // Customizing button appearance (background and text color) based on user interaction
    // https://developer.android.com/reference/com/google/android/material/button/MaterialButton
    private fun setSelectedButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#D1C300"))
        button.setTextColor(Color.BLACK)
    }

    // Customizing button appearance (background and text color) based on user interaction
    // https://developer.android.com/reference/com/google/android/material/button/MaterialButton
    private fun resetButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#FFFCD7"))
        button.setTextColor(Color.BLACK)
    }

    // Firestore add data function adapted from Firebase documentation
    // https://firebase.google.com/docs/firestore/manage-data/add-data
    // https://youtu.be/GEb62UipZi0?si=6hw8rH1IcU-pOMsY
    private fun storeRecipeInFirestore(
        title: String,
        description: String,
        cookingTime: Int,
        difficultyLevel: String,
        cuisineType: List<String>
    ) {
        val recipe = hashMapOf(
            "recipeTitle" to title,
            "recipeDescription" to description,
            "cookingTime" to cookingTime,
            "difficultyLevel" to difficultyLevel,
            "cuisineType" to cuisineType,
            "recipeUserId" to currentUserId,
            "dateRecipeAdded" to Date(),
            "avgRating" to 0.0,
            "comments" to emptyList<Any>()
        )

        firestore.collection("Recipes")
            .add(recipe)
            .addOnSuccessListener {
                // Toast message usage based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
               // https://www.geeksforgeeks.org/how-to-implement-onbackpressed-in-fragments-in-android/
                Toast.makeText(requireContext(), "Recipe added successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { exception ->
                // Toast message usage based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "Failed to add recipe: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group2.recipenest.R
import java.util.*

class AddRecipeFragment : Fragment() {

    // Firebase Firestore instance
    private lateinit var firestore: FirebaseFirestore

    // User ID to be used for storing the recipe
    private val currentUserId = "ceZ4r5FauC7TuTyckeRp"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.add_recipe_fragment, container, false)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Add New Recipe"
        toolbar.setTitleTextColor(Color.BLACK)

        // Initialize UI elements
        val uploadImageButton: Button = rootView.findViewById(R.id.upload_image_button)
        val titleEditText: EditText = rootView.findViewById(R.id.recipe_title)
        val descriptionEditText: EditText = rootView.findViewById(R.id.recipe_description)
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

        // Handle image upload click (currently placeholder functionality)
        uploadImageButton.setOnClickListener {
            Toast.makeText(activity, "Upload image clicked", Toast.LENGTH_SHORT).show()
        }

        // Handle difficulty level toggle selection
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

        // Handle save recipe button click
        saveRecipeButton.setOnClickListener {
            val recipeTitle = titleEditText.text.toString()
            val recipeDescription = descriptionEditText.text.toString()
            val selectedDifficulty = when {
                easyButton.isChecked -> "Easy"
                mediumButton.isChecked -> "Medium"
                hardButton.isChecked -> "Hard"
                else -> "Unknown"
            }

            // Handle cooking time selection from radio group
            val selectedCookingTime = when (cookingTimeGroup.checkedRadioButtonId) {
                R.id.time_15 -> 15
                R.id.time_30 -> 30
                R.id.time_45 -> 45
                R.id.time_60 -> 60
                else -> 0 // Default to 0 if no selection
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

            // Validate input
            if (recipeTitle.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a recipe title", Toast.LENGTH_SHORT).show()
            } else if (selectedCookingTime == 0) {
                Toast.makeText(requireContext(), "Please select a valid cooking time", Toast.LENGTH_SHORT).show()
            } else {
                // Store the recipe in Firestore
                storeRecipeInFirestore(recipeTitle, recipeDescription, selectedCookingTime, selectedDifficulty, cuisineType)
            }
        }

        return rootView
    }

    // Helper to set the selected button state
    private fun setSelectedButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#D1C300")) // Set selected background color
        button.setTextColor(Color.BLACK) // Change text color to black when selected
    }

    // Helper to reset button state to unselected
    private fun resetButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#FFFCD7"))
        button.setTextColor(Color.BLACK)
    }

    // Function to store the recipe in Firestore
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
//            "comments" to listOf(
//                mapOf("breakfast" to emptyList<String>()),
//                mapOf("lunch" to emptyList<String>()),
//                mapOf("snack" to emptyList<String>()),
//                mapOf("dinner" to emptyList<String>())
//            )
        )

        firestore.collection("Recipes")
            .add(recipe)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recipe added successfully!", Toast.LENGTH_SHORT).show()
                // Close the fragment and go back to the previous screen
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to add recipe: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

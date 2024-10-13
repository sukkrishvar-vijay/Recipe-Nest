package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class AddRecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.add_recipe_fragment, container, false)

        // Initialize UI elements
        val uploadImageButton: Button = rootView.findViewById(R.id.upload_image_button)
        val titleEditText: EditText = rootView.findViewById(R.id.recipe_title)
        val descriptionEditText: EditText = rootView.findViewById(R.id.recipe_description)
        val cuisineChinese: CheckBox = rootView.findViewById(R.id.cuisine_chinese)
        val cuisineThai: CheckBox = rootView.findViewById(R.id.cuisine_thai)
        val cuisineAmerican: CheckBox = rootView.findViewById(R.id.cuisine_american)
        val cuisineIndian: CheckBox = rootView.findViewById(R.id.cuisine_indian)
        val cookingTimeGroup: RadioGroup = rootView.findViewById(R.id.cooking_time_group)
        val difficultyGroup: RadioGroup = rootView.findViewById(R.id.difficulty_group)
        val submitButton: Button = rootView.findViewById(R.id.submit_button)

        // Handling image upload (currently just a placeholder)
        uploadImageButton.setOnClickListener {
            Toast.makeText(activity, "Upload image clicked", Toast.LENGTH_SHORT).show()
        }

        // Handling submit button click
        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            val selectedCuisine = mutableListOf<String>()
            if (cuisineChinese.isChecked) selectedCuisine.add("Chinese")
            if (cuisineThai.isChecked) selectedCuisine.add("Thai")
            if (cuisineAmerican.isChecked) selectedCuisine.add("American")
            if (cuisineIndian.isChecked) selectedCuisine.add("Indian")

            val selectedTimeId = cookingTimeGroup.checkedRadioButtonId
            val selectedTime: String = rootView.findViewById<RadioButton>(selectedTimeId).text.toString()

            val selectedDifficultyId = difficultyGroup.checkedRadioButtonId
            val selectedDifficulty: String = rootView.findViewById<RadioButton>(selectedDifficultyId).text.toString()

            // Display selected values (for demonstration)
            val message = "Title: $title\nDescription: $description\nCuisine: $selectedCuisine\nTime: $selectedTime\nDifficulty: $selectedDifficulty"
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        }

        return rootView
    }
}

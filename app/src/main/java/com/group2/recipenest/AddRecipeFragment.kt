package com.group2.recipenest

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.group2.geolocation.LocationHelper
import java.util.*

class AddRecipeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var locationHelper: LocationHelper
    private var recipeUploadLocation: String? = null
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null

    private val currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.add_recipe_fragment, container, false)

        firestore = Firebase.firestore
        storageRef = Firebase.storage.reference

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Add New Recipe"
        toolbar.setTitleTextColor(Color.BLACK)

        val uploadImageButton: Button = rootView.findViewById(R.id.upload_image_button)
        val uploadedImageView: ImageView = rootView.findViewById(R.id.uploaded_image)
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

        locationHelper = LocationHelper(requireContext())
        locationHelper.getCityName { cityName ->
            recipeUploadLocation = cityName ?: "Location not found"
            Toast.makeText(requireContext(), "City: $recipeUploadLocation", Toast.LENGTH_SHORT).show()
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                uploadedImageView.setImageURI(it)
            }
        }

        uploadImageButton.setOnClickListener {
            pickImage.launch("image/*")
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
            val cuisineType = mutableListOf<String>().apply {
                if (cuisineVegetarian.isChecked) add("Vegetarian")
                if (cuisineNonVegetarian.isChecked) add("Non-Vegetarian")
                if (cuisineChinese.isChecked) add("Chinese")
                if (cuisineThai.isChecked) add("Thai")
                if (cuisineIndian.isChecked) add("Indian")
                if (cuisineAmerican.isChecked) add("American")
            }

            if (recipeTitle.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a recipe title", Toast.LENGTH_SHORT).show()
            } else if (selectedCookingTime == 0) {
                Toast.makeText(requireContext(), "Please select a valid cooking time", Toast.LENGTH_SHORT).show()
            } else if (imageUri == null) {
                Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageToFirebase(recipeTitle, recipeDescription, selectedCookingTime, selectedDifficulty, cuisineType)
            }
        }

        return rootView
    }

    private fun uploadImageToFirebase(
        title: String,
        description: String,
        cookingTime: Int,
        difficultyLevel: String,
        cuisineType: List<String>
    ) {
        imageUri?.let { uri ->
            val fileRef = storageRef.child("Recipes/${UUID.randomUUID()}.jpg")
            Log.d("FileRef", fileRef.toString())
            Log.d("uri", uri.toString())

            fileRef.putFile(uri)
                .addOnSuccessListener {
                    // Only after upload success, attempt to get the download URL
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Call storeRecipeInFirestore after both upload and URL retrieval succeed
                        storeRecipeInFirestore(title, description, cookingTime, difficultyLevel, cuisineType, downloadUrl.toString())
                    }.addOnFailureListener { exception ->
                        // Handle download URL retrieval failure
                        Log.d("Exception", "Failed to get image URL: ${exception}")
                        Toast.makeText(requireContext(), "Failed to get image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle file upload failure
                    Log.d("Exception", "Image upload failed: ${exception}")
                    Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
    }


    private fun storeRecipeInFirestore(
        title: String,
        description: String,
        cookingTime: Int,
        difficultyLevel: String,
        cuisineType: List<String>,
        recipeImageUrl: String
    ) {
        val recipe = hashMapOf(
            "recipeTitle" to title,
            "recipeDescription" to description,
            "recipeUploadLocation" to recipeUploadLocation,
            "cookingTime" to cookingTime,
            "difficultyLevel" to difficultyLevel,
            "cuisineType" to cuisineType,
            "recipeUserId" to currentUserId,
            "dateRecipeAdded" to Date(),
            "avgRating" to 0.0,
            "recipeImageUrl" to recipeImageUrl,
            "comments" to emptyList<Any>()
        )

        firestore.collection("Recipes")
            .add(recipe)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recipe added successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to add recipe: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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
}

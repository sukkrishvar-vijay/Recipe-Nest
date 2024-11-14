package com.group2.recipenest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
import com.group2.recipenest.utils.MediaSelector
import java.util.*

class AddRecipeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var locationHelper: LocationHelper
    private var recipeUploadLocation: String? = null
    private lateinit var storageRef: StorageReference
    private lateinit var mediaSelector: MediaSelector
    private var imageUri: Uri? = null
    private var isEditMode = false
    private var recipeId: String? = null
    private var recipeImageUrl: String = ""
    private var existingComments: List<Map<String, Any>> = emptyList()
    private val currentUserId = userSignInData.UserDocId

    private val CAMERA_PERMISSION_REQUEST = 101

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

        // Check permissions on load
        checkAndRequestPermissions()

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

        // Handle edit mode if arguments are provided
        arguments?.let { bundle ->
            isEditMode = true
            toolbar.title = "Edit Recipe"
            saveRecipeButton.text = "Update"
            recipeId = bundle.getString("recipeId")
            if (isEditMode && recipeId != null) {
                fetchExistingComments(recipeId!!)
                fetchExistingRecipeUploadLocation(recipeId!!)
            }
            recipeImageUrl = bundle.getString("recipeImageUrl") ?: ""

            titleEditText.setText(bundle.getString("recipeTitle"))
            descriptionEditText.setText(bundle.getString("recipeDescription"))

            val cuisines = bundle.getString("cuisineType")?.split(", ") ?: emptyList()
            cuisineVegetarian.isChecked = "Vegetarian" in cuisines
            cuisineNonVegetarian.isChecked = "Non-Vegetarian" in cuisines
            cuisineChinese.isChecked = "Chinese" in cuisines
            cuisineThai.isChecked = "Thai" in cuisines
            cuisineIndian.isChecked = "Indian" in cuisines
            cuisineAmerican.isChecked = "American" in cuisines

            when (bundle.getString("difficultyLevel")) {
                "Easy" -> easyButton.isChecked = true
                "Medium" -> mediumButton.isChecked = true
                "Hard" -> hardButton.isChecked = true
            }

            when (bundle.getInt("cookingTime")) {
                15 -> cookingTimeGroup.check(R.id.time_15)
                30 -> cookingTimeGroup.check(R.id.time_30)
                45 -> cookingTimeGroup.check(R.id.time_45)
                60 -> cookingTimeGroup.check(R.id.time_60)
            }

            if (recipeImageUrl.isNotEmpty()) {
                Glide.with(this).load(recipeImageUrl).into(uploadedImageView)
            }
        }

        if(!isEditMode){
            locationHelper = LocationHelper(requireContext())
            locationHelper.getCityName { cityName ->
                recipeUploadLocation = cityName ?: "Location not found"
                Toast.makeText(requireContext(), "City: $recipeUploadLocation", Toast.LENGTH_SHORT).show()
            }
        }

        // Register result launcher to handle camera and gallery results
        val imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) { // From gallery
                        imageUri = data.data
                    } else if (data?.extras?.get("data") != null) { // From camera
                        val bitmap = data.extras!!.get("data") as Bitmap
                        imageUri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                requireContext().contentResolver,
                                bitmap,
                                "Recipe",
                                null
                            )
                        )
                    }
                    uploadedImageView.setImageURI(imageUri) // Display image in the UI
                }
            }

        mediaSelector = MediaSelector(requireContext(), imageResultLauncher)
        uploadImageButton.setOnClickListener { mediaSelector.selectMediaSource() }

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
            } else if (imageUri == null && !isEditMode) {
                Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
            } else {
                if (isEditMode && imageUri == null) {
                    saveRecipeData(
                        recipeTitle,
                        recipeDescription,
                        selectedCookingTime,
                        selectedDifficulty,
                        cuisineType,
                        recipeImageUrl,
                        existingComments
                    )
                } else {
                    uploadImageToFirebase(
                        recipeTitle,
                        recipeDescription,
                        selectedCookingTime,
                        selectedDifficulty,
                        cuisineType
                    )
                }
            }
        }

        return rootView
    }

    private fun checkAndRequestPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("AddRecipeFragment", "Camera and storage permissions granted.")
            } else {
                Toast.makeText(requireContext(), "Camera and storage permissions are required to upload an image.", Toast.LENGTH_SHORT).show()
            }
        }
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
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveRecipeData(
                            title,
                            description,
                            cookingTime,
                            difficultyLevel,
                            cuisineType,
                            downloadUrl.toString(),
                            existingComments
                        )
                    }.addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Failed to get image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: saveRecipeData(
            title,
            description,
            cookingTime,
            difficultyLevel,
            cuisineType,
            recipeImageUrl,
            existingComments
        )
    }

    private fun fetchExistingComments(recipeId: String) {
        firestore.collection("Recipes").document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get existing comments, if any
                    existingComments = document.get("comments") as? List<Map<String, Any>> ?: emptyList()

                    // Get the existing location and store it in recipeUploadLocation
                    recipeUploadLocation = document.getString("recipeUploadLocation") ?: "Location not found"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AddRecipeFragment", "Failed to fetch comments or location: ${exception.message}")
            }
    }

    private fun fetchExistingRecipeUploadLocation(recipeId: String){
        firestore.collection("Recipes").document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the existing location and store it in recipeUploadLocation
                    recipeUploadLocation = document.getString("recipeUploadLocation") ?: "Location not found"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AddRecipeFragment", "Failed to fetch location: ${exception.message}")
            }
    }

    private fun saveRecipeData(
        title: String,
        description: String,
        cookingTime: Int,
        difficultyLevel: String,
        cuisineType: List<String>,
        recipeImageUrl: String,
        comments: List<Map<String, Any>>
    ) {
        val recipeData = hashMapOf(
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
            "comments" to comments
        )

        val recipeDoc = if (isEditMode && recipeId != null) {
            firestore.collection("Recipes").document(recipeId!!)
        } else {
            firestore.collection("Recipes").document()
        }

        recipeDoc.set(recipeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recipe ${if (isEditMode) "updated" else "added"} successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to ${if (isEditMode) "update" else "add"} recipe: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSelectedButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#D1C300"))
        button.setTextColor(Color.BLACK)
    }

    private fun resetButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#FFFCD7"))
        button.setTextColor(Color.BLACK)
    }
}

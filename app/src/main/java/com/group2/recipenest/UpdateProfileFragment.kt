/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class UpdateProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private val userDocumentId = userSignInData.UserDocId
    private val PICK_IMAGE_REQUEST = 1

    private var originalFirstName: String? = null
    private var originalLastName: String? = null
    private var originalUsername: String? = null
    private var originalBio: String? = null
    private var originalAuth: Boolean? = null

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var bioEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var authSwitch: MaterialSwitch
    private lateinit var profileImage: ImageView
    private var imageUri: Uri? = null

    //https://developer.android.com/reference/android/Manifest.permission#READ_EXTERNAL_STORAGE
    //https://developer.android.com/reference/android/Manifest.permission#READ_MEDIA_IMAGES
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.update_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore
        storageReference = Firebase.storage.reference

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Settings"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            handleUnsavedChanges() // Call the confirmation dialog logic when the back arrow is clicked
        }

        profileImage = view.findViewById(R.id.profile_image)
        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        usernameEditText = view.findViewById(R.id.username)
        bioEditText = view.findViewById(R.id.user_bio)
        emailEditText = view.findViewById(R.id.email)
        updateButton = view.findViewById(R.id.update_button)
        authSwitch = view.findViewById(R.id.auth_switch)
        val changeProfileText = view.findViewById<TextView>(R.id.change_profile_text)

        addTextWatcher(firstNameEditText, view.findViewById(R.id.first_name_layout))
        addTextWatcher(lastNameEditText, view.findViewById(R.id.last_name_layout))
        addTextWatcher(usernameEditText, view.findViewById(R.id.username_layout))


        emailEditText.isFocusable = false
        emailEditText.isFocusableInTouchMode = false

        getUserProfileData(userDocumentId)

        updateButton.setOnClickListener {
            var isValid = true

            // First Name Validation
            val firstNameLayout = requireView().findViewById<TextInputLayout>(R.id.first_name_layout)
            if (firstNameEditText.text.isNullOrBlank()) {
                firstNameLayout.helperText = "First Name is required"
                firstNameLayout.setHelperTextColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark))
                isValid = false
            } else {
                firstNameLayout.helperText = null
            }

            // Last Name Validation
            val lastNameLayout = requireView().findViewById<TextInputLayout>(R.id.last_name_layout)
            if (lastNameEditText.text.isNullOrBlank()) {
                lastNameLayout.helperText = "Last Name is required"
                lastNameLayout.setHelperTextColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark))
                isValid = false
            } else {
                lastNameLayout.helperText = null
            }

            // Username Validation
            val usernameLayout = requireView().findViewById<TextInputLayout>(R.id.username_layout)
            if (usernameEditText.text.isNullOrBlank()) {
                usernameLayout.helperText = "Username is required"
                usernameLayout.setHelperTextColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark))
                isValid = false
            } else {
                usernameLayout.helperText = null
            }


            // Check if all fields are valid
            if (!isValid) {
                Toast.makeText(requireContext(), "Please fill out all the required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with the update
            if (imageUri != null) {
                uploadProfileImageAndData()
            } else {
                updateUserProfileDataOnly()
            }
        }
        // Handle swipe gesture/device back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleUnsavedChanges()
                }
            }
        )


        changeProfileText.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), storagePermission) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(storagePermission)
            }
        }
    }

    //https://developer.android.com/reference/android/content/Intent#ACTION_PICK
    //https://developer.android.com/reference/android/provider/MediaStore.Images.Media
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    //https://developer.android.com/reference/android/content/Intent#getData()
    //https://github.com/bumptech/glide
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.placeholder_avatar_image)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }

    //function to get user profile details from firebase firestore
    //https://firebase.google.com/docs/firestore/query-data/get-data
    //https://github.com/bumptech/glide
    private fun getUserProfileData(userId: String) {
        val userRef = firestore.collection("User").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                originalFirstName = document.getString("firstName")
                originalLastName = document.getString("lastName")
                originalUsername = document.getString("username")
                originalBio = document.getString("bio")
                val email = document.getString("email")
                originalAuth = document.getBoolean("biometricEnabled")
                val profileImageUrl = document.getString("profileImageUrl")

                firstNameEditText.setText(originalFirstName)
                lastNameEditText.setText(originalLastName)
                usernameEditText.setText(originalUsername)
                bioEditText.setText(originalBio)
                authSwitch.isChecked = originalAuth ?: false
                emailEditText.setText(email)

                profileImageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .placeholder(R.drawable.placeholder_avatar_image)
                        .circleCrop()
                        .into(profileImage)
                }
            } else {
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
    }

    //https://firebase.google.com/docs/storage/android/upload-files
    private fun uploadProfileImageAndData() {
        val filePath = storageReference.child("User_Profiles/$userDocumentId.jpg")

        imageUri?.let { uri ->
            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { downloadUrl ->
                        updateUserProfileDataOnly(downloadUrl.toString())
                        imageUri = null
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //function to update firebase with updated details
    //https://firebase.google.com/docs/firestore/manage-data/add-data
    private fun updateUserProfileDataOnly(profileImageUrl: String = "") {
        val updatedFirstName = firstNameEditText.text.toString()
        val updatedLastName = lastNameEditText.text.toString()
        val updatedUsername = usernameEditText.text.toString()
        val updatedBio = bioEditText.text.toString()
        val auth = authSwitch.isChecked

        val userRef = firestore.collection("User").document(userDocumentId)

        val updates = mutableMapOf(
            "firstName" to updatedFirstName,
            "lastName" to updatedLastName,
            "username" to updatedUsername,
            "bio" to updatedBio,
            "biometricEnabled" to auth
        )

        if (profileImageUrl.isNotEmpty()) {
            updates["profileImageUrl"] = profileImageUrl
        }

        //https://developer.android.com/reference/android/content/SharedPreferences
        //https://developer.android.com/reference/android/content/SharedPreferences.Editor#putBoolean(java.lang.String,%20boolean)
        val sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("biometricEnabled", auth).apply()

        userRef.update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                originalFirstName = updatedFirstName
                originalLastName = updatedLastName
                originalUsername = updatedUsername
                originalBio = updatedBio
                originalAuth = auth
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to handle unsaved changes and show the confirmation dialog
    private fun handleUnsavedChanges() {
        if (hasUnsavedChanges()) {
            showDiscardChangesDialog()
        } else {
            navigateToProfileFragment()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        return firstNameEditText.text.toString() != originalFirstName ||
                lastNameEditText.text.toString() != originalLastName ||
                usernameEditText.text.toString() != originalUsername ||
                bioEditText.text.toString() != originalBio ||
                authSwitch.isChecked != (originalAuth ?: false) ||
                imageUri != null
    }

    // Method to show a confirmation dialog
    private fun showDiscardChangesDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Yes") { _, _ -> navigateToProfileFragment() }
            .setNegativeButton("No", null)
            .create() 

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    // Navigate back to ProfileFragment
    private fun navigateToProfileFragment() {
        parentFragmentManager.popBackStack() // Go back to the previous fragment in the back stack
    }

    private fun addTextWatcher(editText: TextInputEditText, layout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    layout.helperText = "${layout.hint} is required"
                    layout.setHelperTextColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark))
                } else {
                    layout.helperText = null
                }
            }
        })
    }


}
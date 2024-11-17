/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
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

        emailEditText.isFocusable = false
        emailEditText.isFocusableInTouchMode = false

        getUserProfileData(userDocumentId)

        updateButton.setOnClickListener {
            if (imageUri != null) {
                uploadProfileImageAndData()
            } else {
                updateUserProfileDataOnly()
            }
        }

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
}

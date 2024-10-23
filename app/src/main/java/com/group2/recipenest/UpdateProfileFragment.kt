package com.group2.recipenest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UpdateProfileFragment : Fragment() {

    // Firestore instance
    private lateinit var firestore: FirebaseFirestore

    // User ID for querying the Firestore
    private val userDocumentId = userSignInData.UserDocId

    // To store original data for detecting unsaved changes
    private var originalFirstName: String? = null
    private var originalLastName: String? = null
    private var originalUsername: String? = null
    private var originalBio: String? = null

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var bioEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var updateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.update_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        firestore = Firebase.firestore

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title directly
        toolbar.title = "Settings"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set up the back button (up button)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Access UI elements
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        usernameEditText = view.findViewById(R.id.username)
        bioEditText = view.findViewById(R.id.user_bio)
        emailEditText = view.findViewById(R.id.email) // Email EditText
        updateButton = view.findViewById(R.id.update_button)
        val authSwitch = view.findViewById<MaterialSwitch>(R.id.auth_switch)

        // Set email field as read-only
        emailEditText.isEnabled = false

        // Query Firestore to get user profile data
        getUserProfileData(userDocumentId)

        // Set OnClickListener for Update button
        updateButton.setOnClickListener {
            updateUserProfile()
        }

        // Handle biometric authentication toggle and save locally
        val sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE)
        val biometricEnabled = sharedPreferences.getBoolean("biometricEnabled", false)
        authSwitch.isChecked = biometricEnabled

        authSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("biometricEnabled", isChecked).apply()
            if (isChecked) {
                Toast.makeText(requireContext(), "Biometric Auth Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Biometric Auth Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to query Firestore and fetch user profile data
    private fun getUserProfileData(userId: String) {
        val userRef = firestore.collection("User").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Extract the data and populate EditText fields
                originalFirstName = document.getString("firstName")
                originalLastName = document.getString("lastName")
                originalUsername = document.getString("username")
                originalBio = document.getString("bio")
                val email = document.getString("email") // Fetch the email from Firestore

                firstNameEditText.setText(originalFirstName)
                lastNameEditText.setText(originalLastName)
                usernameEditText.setText(originalUsername)
                bioEditText.setText(originalBio)
                emailEditText.setText(email) // Set email in read-only field
            } else {
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update user profile in Firestore
    private fun updateUserProfile() {
        val updatedFirstName = firstNameEditText.text.toString()
        val updatedLastName = lastNameEditText.text.toString()
        val updatedUsername = usernameEditText.text.toString()
        val updatedBio = bioEditText.text.toString()

        val userRef = firestore.collection("User").document(userDocumentId)

        val updates = mapOf(
            "firstName" to updatedFirstName,
            "lastName" to updatedLastName,
            "username" to updatedUsername,
            "bio" to updatedBio
        )

        userRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                // Update original values after saving
                originalFirstName = updatedFirstName
                originalLastName = updatedLastName
                originalUsername = updatedUsername
                originalBio = updatedBio
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}

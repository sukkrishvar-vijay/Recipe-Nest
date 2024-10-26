/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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

    private lateinit var firestore: FirebaseFirestore

    private val userDocumentId = userSignInData.UserDocId

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

        firestore = Firebase.firestore

        // Setting up Toolbar with navigation icon and handling click events based on Android developer documentation
        // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
        // https://developer.android.com/reference/androidx/activity/OnBackPressedDispatcher
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Settings"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        usernameEditText = view.findViewById(R.id.username)
        bioEditText = view.findViewById(R.id.user_bio)
        emailEditText = view.findViewById(R.id.email)
        updateButton = view.findViewById(R.id.update_button)
        val authSwitch = view.findViewById<MaterialSwitch>(R.id.auth_switch)

        // Disabling EditText fields to prevent modification based on Android developer documentation
        // https://developer.android.com/reference/android/widget/EditText
        // https://kotlinlang.org/api/latest/jvm/stdlib/org.khronos.webgl/-web-g-l-rendering-context-base/is-enabled.html
        emailEditText.isEnabled = false

        getUserProfileData(userDocumentId)

        updateButton.setOnClickListener {
            updateUserProfile()
        }

        // Storing user preferences with SharedPreferences based on Android developer documentation
        // https://developer.android.com/training/data-storage/shared-preferences
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/
        val sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE)
        val biometricEnabled = sharedPreferences.getBoolean("biometricEnabled", false)
        authSwitch.isChecked = biometricEnabled

        authSwitch.setOnCheckedChangeListener {_, isChecked ->
            sharedPreferences.edit().putBoolean("biometricEnabled", isChecked).apply()
            if (isChecked) {
                // Displaying feedback to the user using Toast messages based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "Biometric Auth Enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Displaying feedback to the user using Toast messages based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "Biometric Auth Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firestore data retrieval and document handling based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data

    private fun getUserProfileData(userId: String) {
        val userRef = firestore.collection("User").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                originalFirstName = document.getString("firstName")
                originalLastName = document.getString("lastName")
                originalUsername = document.getString("username")
                originalBio = document.getString("bio")
                val email = document.getString("email")

                firstNameEditText.setText(originalFirstName)
                lastNameEditText.setText(originalLastName)
                usernameEditText.setText(originalUsername)
                bioEditText.setText(originalBio)
                emailEditText.setText(email)
            } else {
                // Displaying feedback to the user using Toast messages based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Displaying feedback to the user using Toast messages based on Android developer documentation
            // https://developer.android.com/guide/topics/ui/notifiers/toasts
            Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
    }

    // Firestore document update with map data based on Firebase documentation
    // https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-string.html
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
                // Displaying feedback to the user using Toast messages based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts

                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                originalFirstName = updatedFirstName
                originalLastName = updatedLastName
                originalUsername = updatedUsername
                originalBio = updatedBio
            }
            .addOnFailureListener {
                // Displaying feedback to the user using Toast messages based on Android developer documentation
                // https://developer.android.com/guide/topics/ui/notifiers/toasts
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}

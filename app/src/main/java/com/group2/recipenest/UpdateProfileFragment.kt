package com.group2.recipenest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UpdateProfileFragment : Fragment() {

    // Firestore instance
    private lateinit var firestore: FirebaseFirestore

    // User ID for querying the Firestore
    private val userDocumentId = "ceZ4r5FauC7TuTyckeRp"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)  // Replace with your back icon
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()  // Navigate back when back button is clicked
        }

        // Access UI elements
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val firstNameEditText = view.findViewById<EditText>(R.id.first_name)
        val lastNameEditText = view.findViewById<EditText>(R.id.last_name)
        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val bioEditText = view.findViewById<EditText>(R.id.user_bio)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val authSwitch = view.findViewById<SwitchMaterial>(R.id.auth_switch)
        val updateButton = view.findViewById<Button>(R.id.update_button)

        // Query Firestore when the page is loaded to get user profile data
        getUserProfileData(userDocumentId, firstNameEditText, lastNameEditText, usernameEditText, bioEditText, emailEditText)

        // Set OnClickListener for Update button (example functionality)
        updateButton.setOnClickListener {
            Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
        }

        // Set Switch functionality (example)
        authSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Biometric Auth Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Biometric Auth Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to query Firestore and fetch user profile data
    private fun getUserProfileData(
        userId: String,
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        usernameEditText: EditText,
        bioEditText: EditText,
        emailEditText: EditText
    ) {
        val userRef = firestore.collection("User").document(userId)

        // Get the document with the userId
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Extract the data from the document and populate the EditText fields
                firstNameEditText.setText(document.getString("firstName"))
                lastNameEditText.setText(document.getString("lastName"))
                usernameEditText.setText(document.getString("username"))
                bioEditText.setText(document.getString("bio"))
                emailEditText.setText(document.getString("email"))
            } else {
                // Handle case where document does not exist
                Log.d("UpdateProfileFragment", "No such user document found")
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Handle errors in getting document
            Log.e("UpdateProfileFragment", "Error fetching user data", exception)
            Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.group2.recipenest

import android.graphics.Color
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
import com.google.android.material.switchmaterial.SwitchMaterial

class UpdateProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.update_profile, container, false)

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
        val profileImage = rootView.findViewById<ImageView>(R.id.profile_image)
        val firstNameEditText = rootView.findViewById<EditText>(R.id.first_name)
        val lastNameEditText = rootView.findViewById<EditText>(R.id.last_name)
        val usernameEditText = rootView.findViewById<EditText>(R.id.username)
        val bioEditText = rootView.findViewById<EditText>(R.id.user_bio)
        val emailEditText = rootView.findViewById<EditText>(R.id.email)
        val passwordEditText = rootView.findViewById<EditText>(R.id.password)
        val authSwitch = rootView.findViewById<SwitchMaterial>(R.id.auth_switch)
        val updateButton = rootView.findViewById<Button>(R.id.update_button)

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

        return rootView
    }
}

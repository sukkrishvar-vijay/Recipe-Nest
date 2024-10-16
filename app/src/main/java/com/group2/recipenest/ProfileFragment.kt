package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.group2.recipenest.com.group2.recipenest.SignInFragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title directly
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set click listener on "My Recipes" tile (list_item_title1)
        val myRecipesTile = rootView.findViewById<TextView>(R.id.list_item_title1)
        myRecipesTile.setOnClickListener {
            // Navigate to MyRecipesFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyRecipesFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set click listener on "Update Profile and Settings" tile (list_item_tile2)
        val updateProfileAndSettingsTile = rootView.findViewById<TextView>(R.id.list_item_title2)
        updateProfileAndSettingsTile.setOnClickListener{
            // Navigate to UpdateProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UpdateProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        val logoutTile = rootView.findViewById<TextView>(R.id.list_item_title3)
        logoutTile.setOnClickListener {
            // Navigate to SignInFragment
            logOutUser()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment())
                .commit()
        }

        return rootView
    }

    private fun logOutUser() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut()
        // Optionally, redirect to the login page or show a confirmation message
        Toast.makeText(requireContext(), "You have been logged out.", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        // Reset the toolbar when FavoritesFragment is resumed
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Remove the navigation icon (back button)
        toolbar.navigationIcon = null  // This removes the back button from the toolbar
    }
}

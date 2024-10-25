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

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Toolbar title setup and customization learned from Android developer documentation
        // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        val myRecipesTile = rootView.findViewById<TextView>(R.id.list_item_title1)
        myRecipesTile.setOnClickListener {
            // Fragment transaction and navigation based on Android developer documentation
            // https://developer.android.com/guide/fragments/fragmentmanager
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyRecipesFragment())
                .addToBackStack(null)
                .commit()
        }

        val updateProfileAndSettingsTile = rootView.findViewById<TextView>(R.id.list_item_title2)
        updateProfileAndSettingsTile.setOnClickListener{
            // Fragment transaction and navigation based on Android developer documentation
            // https://developer.android.com/guide/fragments/fragmentmanager
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UpdateProfileFragment())

                // Handling back navigation in fragment transactions adapted from Android developer guide
                // https://developer.android.com/guide/navigation/navigation-custom-back
                .addToBackStack(null)
                .commit()
        }

        val logoutTile = rootView.findViewById<TextView>(R.id.list_item_title3)
        logoutTile.setOnClickListener {
            logOutUser()

            // Fragment transaction and navigation based on Android developer documentation
            // https://developer.android.com/guide/fragments/fragmentmanager
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment())
                .commit()
        }

        return rootView
    }

    // FirebaseAuth sign out logic adapted from Firebase documentation
    // https://firebase.google.com/docs/auth/android/password-auth
    private fun logOutUser() {
        FirebaseAuth.getInstance().signOut()
        clearSignInUserData()

        // Toast messages implementation based on Android developer guide
        // https://developer.android.com/guide/topics/ui/notifiers/toasts
        Toast.makeText(requireContext(), "You have been logged out.", Toast.LENGTH_SHORT).show()
    }

    private fun clearSignInUserData() {
        userSignInData.UserUID = ""
        userSignInData.UserDocId = ""
    }

    override fun onResume() {
        super.onResume()

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Account"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.navigationIcon = null
    }
}
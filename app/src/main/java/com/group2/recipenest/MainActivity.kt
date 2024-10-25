/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group2.recipenest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change the status bar color
        window.statusBarColor = getColor(R.color.theme)

        // Find the Toolbar and set it as the ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.hide()

        // Disable the back button in the toolbar (enable where required)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        if (isFirstLaunch()) {
            hideBottomNavigation()
            // Load the landing pages fragment/activity
            loadFragment(LandingPage1())
        } else {
            if (!isUserLoggedIn()) {
                hideBottomNavigation()
                // Load the sign-in fragment if the user is not logged in
                signInAccount()
            } else {
                // If logged in, load the default fragment (home page)
                loadHomePage()
            }
        }
    }

    private fun isFirstLaunch(): Boolean {
        val sharedPreferences = getSharedPreferences("RecipeNestPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFirstLaunch", true)
    }

    // Method to show bottom navigation
    fun showBottomNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    // Method to hide Toolbar
    fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    // Method to hide bottom navigation
    fun hideBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE
    }

    // Method to navigate to Sign in fragment
    private fun signInAccount() {
        loadFragment(SignInFragment())
    }

    // Method to load homepage
    fun loadHomePage() {
        storeUserDocId()
        showBottomNavigation()

        // Load the default fragment
        loadFragment(RecipesFragment())

        // Set the BottomNavigationView to show Recipes as selected by default
        binding.bottomNavigation.selectedItemId = R.id.nav_recipes

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recipes -> loadFragment(RecipesFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_favorites -> loadFragment(FavoritesFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    // Helper function to check User Login Status
    private fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userSignInData.UserUID = currentUser.uid
        }
        return currentUser != null
    }

    // Method to search database and save user document id in the data class
    private fun storeUserDocId() {
        db.collection("User")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val documentUserUID = document.getString("userUID")
                    if (userSignInData.UserUID == documentUserUID) {
                        userSignInData.UserDocId = document.id
                        Log.d("USERDOCID", document.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("USERDOCID", "error", e)
            }
    }

    // Helper function to load fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

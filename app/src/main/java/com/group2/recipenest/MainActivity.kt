/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.content.Context
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

        window.statusBarColor = getColor(R.color.theme)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.hide()

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        if (isFirstLaunch()) {
            hideBottomNavigation()
            loadFragment(LandingPage1())
        } else {
            if (!isUserLoggedIn()) {
                hideBottomNavigation()
                signInAccount()
            } else {
                userSignInData.ShowAuthFirstTime = false
                loadHomePage()
            }
        }
    }

    // Function to check for first launch  and show landing pages
    //https://stackoverflow.com/questions/5950043/how-to-use-getsharedpreferences-in-android
    private fun isFirstLaunch(): Boolean {
        val sharedPreferences = getSharedPreferences("RecipeNestPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFirstLaunch", true)
    }

    fun showBottomNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    fun hideBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun signInAccount() {
        loadFragment(SignInFragment())
    }

    fun loadHomePage() {
        storeUserDocId()
        showBottomNavigation()

        showBottomNavigation()
        loadFragment(RecipesFragment())

        binding.bottomNavigation.selectedItemId = R.id.nav_recipes

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
    //https://firebase.google.com/docs/auth/android/manage-users
    //https://stackoverflow.com/questions/45010081/why-firebaseauth-getinstance-getcurrentuser-is-returning-null-value-in-andro
    private fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userSignInData.UserUID = currentUser.uid
        }
        return currentUser != null
    }

    // Method to search database and save user document id in the data class
    //https://firebase.google.com/docs/firestore/query-data/get-data#kotlin+ktx_8
    //https://iamvs2002.hashnode.dev/cloud-firestore-in-android-studio
    private fun storeUserDocId() {
        db.collection("User")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val documentUserUID = document.getString("userUID")
                    val documentBiometricEnabled = document.getBoolean("biometricEnabled")
                    if (userSignInData.UserUID == documentUserUID) {
                        userSignInData.UserDocId = document.id
                        Log.d("USERDOCID", document.id)
                        val sharedPreferences = getSharedPreferences("UserSettings", Context.MODE_PRIVATE)
                        if (documentBiometricEnabled != null) {
                            sharedPreferences.edit().putBoolean("biometricEnabled", documentBiometricEnabled).apply()
                            Log.d("USERDOCID", documentBiometricEnabled.toString())
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("USERDOCID", "error", e)
            }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

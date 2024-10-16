package com.group2.recipenest
//vijay develops
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.group2.recipenest.com.group2.recipenest.SignInFragment
import com.group2.recipenest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Find the Toolbar and set it as the ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Disable the back button in the toolbar (enable where required)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        if (!isUserLoggedIn()) {
            hideBottomNavigation()
            // Load the sign-in fragment if the user is not logged in
            loadFragment(SignInFragment())
        } else {
            // If logged in, load the default fragment (home page)
            loadHomePage()
        }

    }

    // Method to show bottom navigation
    private fun showBottomNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    // Method to hide bottom navigation
    public fun hideBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE
    }

    public fun loadHomePage(){
        // Load the default fragment
        showBottomNavigation()
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
        // Get the current user
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if the user is signed in (not null)
        return currentUser != null
    }

    // Helper function to load fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

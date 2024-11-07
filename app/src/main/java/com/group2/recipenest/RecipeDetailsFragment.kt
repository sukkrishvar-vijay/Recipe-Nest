/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.Manifest
import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import android.provider.Settings
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecipeDetailsFragment : Fragment() {

    private lateinit var avgRatingTextView: TextView
    private lateinit var shortDetailTextView: TextView
    private lateinit var longDetailTextView: TextView
    private lateinit var recipeOwnerTextView: TextView
    private lateinit var ratingsCommentsButton: Button
    private lateinit var favoriteButton: ImageButton
    private lateinit var currentRecipeId: String
    private lateinit var firestore: FirebaseFirestore
    private var isFavorite = false
    private var currentFavoriteCategory: String? = null

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var currentLocation: Location? = null

    private val currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.recipe_detail, container, false)

        firestore = Firebase.firestore

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        recipeOwnerTextView = rootView.findViewById(R.id.ownerNameAndUsername)
        avgRatingTextView = rootView.findViewById(R.id.ratingText)
        shortDetailTextView = rootView.findViewById(R.id.selectedFilters)
        longDetailTextView = rootView.findViewById(R.id.aboutRecipeDetails)
        ratingsCommentsButton = rootView.findViewById(R.id.ratingsCommentsButton)
        favoriteButton = rootView.findViewById(R.id.favoriteButton)

        setRecipeDetails()

        setUpToolbarWithBackButton()

        // FloatingActionButton click handling and fragment navigation based on Android developer documentation
        // https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton
        //https://discuss.kotlinlang.org/t/trying-to-understand-onclicklistener/24773
        val fabWriteComment: FloatingActionButton = rootView.findViewById(R.id.fab_write_comment)
        fabWriteComment.setOnClickListener {
            navigateToPostCommentFragment()
        }

        fetchAndSetCommentsAndAvgRating()

        checkIfFavorite(currentRecipeId)

        favoriteButton.setOnClickListener {
            showFavoriteDialog(currentRecipeId)
        }

        ratingsCommentsButton.setOnClickListener {
            openReviewFragment()
        }

        // Trigger the location fetching process
        getLocation()

        return rootView
    }

    // Retrieving fragment arguments and updating UI elements based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-type/arguments.html
    private fun setRecipeDetails() {
        val arguments = arguments ?: return

        val recipeTitle = arguments.getString("recipeTitle") ?: "Recipe Details"
        val recipeOwner = arguments.getString("recipeUserId") ?: "Unknown"
        val recipeDescription = arguments.getString("recipeDescription") ?: "No description available"
        val difficultyLevel = arguments.getString("difficultyLevel") ?: "Unknown"
        val cookingTime = arguments.getInt("cookingTime", 0)
        val cuisineType = arguments.getString("cuisineType") ?: "Unknown"
        currentRecipeId = arguments.getString("recipeId") ?: "Unknown"

        fetchAndSetRecipeOwnerDetails(recipeOwner)

        shortDetailTextView.text = "$difficultyLevel • $cookingTime mins • $cuisineType"
        longDetailTextView.text = recipeDescription

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = recipeTitle
    }

    // Firestore document retrieval and setting text views with data based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://developer.android.com/reference/com/google/android/play/core/tasks/OnFailureListener
    private fun fetchAndSetRecipeOwnerDetails(recipeUserId: String) {
        val userRef = firestore.collection("User").document(recipeUserId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val username = document.getString("username") ?: ""
                recipeOwnerTextView.text = "$firstName $lastName • $username"
            } else {
                recipeOwnerTextView.text = "Unknown User"
            }
        }.addOnFailureListener {
            recipeOwnerTextView.text = "Error loading user"
        }
    }

    // Firestore document retrieval, querying, and updates based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://docs.airship.com/reference/libraries/android-kotlin/latest/urbanairship-core/com.urbanairship.actions/-action-value/get-double.html
    private fun fetchAndSetCommentsAndAvgRating() {
        val recipeRef = firestore.collection("Recipes").document(currentRecipeId)
        recipeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val comments = document.get("comments") as? List<Map<String, Any>>
                val commentCount = comments?.size ?: 0
                ratingsCommentsButton.text = "Ratings and Comments ($commentCount)"

                val avgRating = document.getDouble("avgRating") ?: 0.0
                avgRatingTextView.text = "${avgRating}★"
            } else {
                ratingsCommentsButton.text = "Ratings and Comments (0)"
                avgRatingTextView.text = "N/A★"
            }
        }.addOnFailureListener {
            ratingsCommentsButton.text = "Ratings and Comments (0)"
            avgRatingTextView.text = "N/A★"
        }
    }

    // Firestore document retrieval and checking conditions within nested collections based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/for-each.html
    private fun checkIfFavorite(recipeId: String) {
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favorites = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                favorites?.forEach { categoryMap ->
                    categoryMap.forEach { (category, recipeIds) ->
                        if (recipeIds.contains(recipeId)) {
                            isFavorite = true
                            currentFavoriteCategory = category
                        }
                    }
                }
                updateFavoriteIcon(isFavorite)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to check favorites", Toast.LENGTH_SHORT).show()
        }
    }

    // Toolbar customization and back navigation based on Android developer guide
    // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
    // https://williamrai.hashnode.dev/difference-between-requireactivity-and-requirecontext
    private fun setUpToolbarWithBackButton() {
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showFavoriteDialog(recipeId: String) {
        val userRef = firestore.collection("User").document(currentUserId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val favoriteCategories = favoriteCollection?.mapNotNull { it.keys.firstOrNull() } ?: emptyList()

                // AlertDialog setup and single-choice handling based on Android developer documentation
                // https://developer.android.com/reference/androidx/appcompat/app/AlertDialog
                // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-typed-array.html
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select Favorite Category")

                val options = favoriteCategories.toTypedArray()
                var selectedOption: String? = currentFavoriteCategory

                // AlertDialog with single-choice items setup and selection handling based on Android developer documentation
                // https://developer.android.com/reference/androidx/appcompat/app/AlertDialog.Builder#setSingleChoiceItems(java.lang.CharSequence[],%20int,%20android.content.DialogInterface.OnClickListener)
                // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/index-of.html
                builder.setSingleChoiceItems(options, options.indexOf(currentFavoriteCategory)) { _, which ->
                    selectedOption = options[which]
                }

                // AlertDialog with button click handling and conditional logic based on Android developer documentation
                // https://developer.android.com/reference/androidx/appcompat/app/AlertDialog
                // https://stackoverflow.com/questions/19286135/android-alert-dialog-and-set-positive-button
                builder.setPositiveButton("Save") { _, _ ->
                    if (!selectedOption.isNullOrEmpty()) {
                        if (currentFavoriteCategory != null && currentFavoriteCategory != selectedOption) {
                            removeRecipeFromCurrentCategory(recipeId, currentFavoriteCategory!!) {
                                addRecipeToFavoriteCategory(recipeId, selectedOption!!)
                            }
                        } else if (currentFavoriteCategory == null) {
                            addRecipeToFavoriteCategory(recipeId, selectedOption!!)
                        }
                    }
                }

                builder.setNegativeButton("Cancel", null)
                builder.create().show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load favorite categories", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addRecipeToFavoriteCategory(recipeId: String, category: String) {
        firestore.collection("User").document(currentUserId).get().addOnSuccessListener { document ->
            if (document.exists()) {

                // Firestore document retrieval and updating nested collections based on Firebase documentation
                // https://firebase.google.com/docs/firestore/query-data/get-data
                // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>
                val updatedFavorites = favoriteCollection?.map {
                    if (it.containsKey(category)) {
                        it.toMutableMap().apply {
                            this[category] = this[category]!!.plus(recipeId)
                        }
                    } else {
                        it
                    }
                }?.toList()

                firestore.collection("User").document(currentUserId)
                    .update("favoriteCollection", updatedFavorites)
                    .addOnSuccessListener {
                        isFavorite = true
                        currentFavoriteCategory = category
                        updateFavoriteIcon(isFavorite)
                        Toast.makeText(requireContext(), "Added to $category", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add favorite", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to retrieve favorite categories", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeRecipeFromCurrentCategory(recipeId: String, category: String, onComplete: () -> Unit) {
        val userRef = firestore.collection("User").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {

                // Firestore document retrieval and list modification for nested collections, based on Firebase documentation
                // https://firebase.google.com/docs/firestore/query-data/get-data
                // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
                val favoriteCollection = document.get("favoriteCollection") as? List<Map<String, List<String>>>

                if (favoriteCollection != null) {
                    val updatedFavorites = favoriteCollection.map { categoryMap ->
                        if (categoryMap.containsKey(category)) {
                            categoryMap.toMutableMap().apply {
                                this[category] = this[category]!!.filterNot { it == recipeId }
                            }
                        } else {
                            categoryMap
                        }
                    }

                    userRef.update("favoriteCollection", updatedFavorites)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Removed from $category", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to retrieve favorite categories", Toast.LENGTH_SHORT).show()
        }
    }

    // Passing data between fragments using Bundle based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    private fun openReviewFragment() {
        val reviewFragment = ReviewFragment()

        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        reviewFragment.arguments = bundle

        reviewFragment.show(requireActivity().supportFragmentManager, reviewFragment.tag)
    }


    // Updating ImageButton appearance dynamically based on selection, adapted from Android developer documentation
    // https://developer.android.com/reference/android/widget/ImageButton#setImageResource(int)
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_outline)
        }
    }

    override fun onResume() {
        super.onResume()
        setRecipeDetails()
        fetchAndSetCommentsAndAvgRating()
    }

    // Passing data between fragments using Bundle based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    private fun navigateToPostCommentFragment() {
        val postCommentFragment = PostCommentFragment()
        val bundle = Bundle()
        bundle.putString("recipeId", currentRecipeId)
        postCommentFragment.arguments = bundle

        // Fragment transactions and navigation pattern based on Android developer documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, postCommentFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getLocation() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        var locationByGps: Location? = null
        var locationByNetwork: Location? = null

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
                updateLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork = location
                updateLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasGps || hasNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permissions if not granted
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1010
                )
                return
            }

            if (hasGps) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    gpsLocationListener
                )
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    networkLocationListener
                )
            }

            // Use the most accurate location
            if (locationByGps != null && locationByNetwork != null) {
                currentLocation = if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) locationByGps else locationByNetwork
            } else {
                currentLocation = locationByGps ?: locationByNetwork
            }

            currentLocation?.let {
                latitude = it.latitude
                longitude = it.longitude
                Log.d("Location", "Current Location: Lat $latitude, Long $longitude")
            }

        } else {
            // Prompt user to enable location
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun updateLocation(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        Log.d("Location", "Updated Location: Lat $latitude, Long $longitude")
    }
}
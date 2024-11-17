/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
import android.location.Geocoder

// LocationHelper class is used in RecipesFragment page to filter out recipes based on the user's current location
// This class is also used in AddRecipeFragment page to record the current location of the user from where the user is uploading a recipe
// https://www.youtube.com/watch?v=mC1VVHmgKXI&ab_channel=Dr.ParagShukla
// https://stackoverflow.com/questions/4013606/google-maps-how-to-get-country-state-province-region-city-given-a-lat-long-va
class LocationHelper(private val context: Context) {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val PERMISSION_REQUEST_ACCESS_LOCATION = 100

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getCityName(callback: (cityName: String?) -> Unit) {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                try {
                    fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                        val location: Location? = task.result
                        if (location != null) {
                            callback(fetchCityName(location.latitude, location.longitude))
                        } else {
                            callback(null)
                            Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    callback(null)
                    Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Turn on Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        } else {
            requestPermission()
            callback(null)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Handle location permission
    // https://developer.android.com/develop/sensors-and-location/location/permissions
    // https://medium.com/@aman1024/handling-location-permission-in-android-kotlin-a1bc4c1cd9da
    private fun requestPermission() {
        if (context is androidx.fragment.app.FragmentActivity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_ACCESS_LOCATION
            )
        }
    }

    // Handle location permission
    // https://developer.android.com/develop/sensors-and-location/location/permissions
    // https://medium.com/@aman1024/handling-location-permission-in-android-kotlin-a1bc4c1cd9da
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Get the city name of the passed coordinates
    // https://stackoverflow.com/questions/24310594/get-only-the-city-name-from-coordinates-in-android/52024163
    // https://www.geeksforgeeks.org/how-to-get-city-name-by-using-geolocation/
    private fun fetchCityName(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality ?: "City not found"
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

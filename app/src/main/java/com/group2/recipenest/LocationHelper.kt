package com.group2.geolocation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
import android.location.Geocoder

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

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchCityName(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality ?: "City not found"
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("LocationHelper", "Geocoder failed: ${e.message}")
            null
        }
    }
}

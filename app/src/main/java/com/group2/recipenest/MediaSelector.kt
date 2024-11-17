/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest.utils

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog

class MediaSelector(private val context: Context, private val activityResultLauncher: ActivityResultLauncher<Intent>) {

    // Displays a dialog for the user to choose between the camera or the gallery as the media source.
    // https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
    // https://developer.android.com/reference/android/app/AlertDialog.Builder
    fun selectMediaSource() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }
    // Launches the Camera App for Capturing an image
    // https://developer.android.com/media/camera/camera-intents
    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncher.launch(intent)
    }
    // Opens the gallery for the user to select an image from their device
    // https://developer.android.com/training/data-storage/shared/media
    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }
}

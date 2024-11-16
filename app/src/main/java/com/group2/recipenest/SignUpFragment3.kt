/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group2.recipenest.databinding.CreateAccount3Binding

class SignUpFragment3 : Fragment() {
    private var _binding: CreateAccount3Binding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var selectedImageUri: Uri? = null
    private var current_user: FirebaseUser? = null

    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.landingpageImg2)
                Toast.makeText(requireContext(), "Image selected and displayed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Image selection failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            selectImageFromGallery()
        } else {
            Toast.makeText(requireContext(), "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateAccount3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.uploadImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), storagePermission) == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery()
            } else {
                requestPermissionLauncher.launch(storagePermission)
            }
        }

        binding.previousButton.setOnClickListener {
            loadFragment(SignUpFragment2())
        }

        binding.submitButton.setOnClickListener {
            val email = userData.email
            val password = userData.password

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        current_user = auth.currentUser
                        Toast.makeText(requireContext(), "Success!", Toast.LENGTH_SHORT).show()
                        uploadImageToFirebase { imageUrl ->
                            userData.profileimage = imageUrl
                            writeUserToFirebase(current_user?.uid)
                            userSignInData.UserUID = current_user!!.uid
                            loadFragment(AccountCreatedFragment())
                        }
                    } else {
                        Log.e("SignUpFragment3", "Authentication Failed: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(onUploadComplete: (String) -> Unit) {
        if (selectedImageUri != null) {
            val storageRef = Firebase.storage.reference.child("User_Profiles/${userData.email}_profile.jpg")
            val uploadTask = storageRef.putFile(selectedImageUri!!)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onUploadComplete(downloadUri.toString())
                    Log.d("SignUpFragment3", "Image uploaded successfully: $downloadUri")
                    Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("SignUpFragment3", "Image upload failed: ${e.message}", e)
                Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            onUploadComplete("")
        }
    }

    private fun writeUserToFirebase(userId: String?) {
        val user = hashMapOf(
            "firstName" to userData.firstName,
            "lastName" to userData.lastName,
            "email" to userData.email,
            "username" to userData.username,
            "bio" to userData.description,
            "profileImageUrl" to userData.profileimage,
            "userUID" to userId,
            "favoriteCollection" to listOf(
                mapOf("breakfast" to emptyList<String>()),
                mapOf("lunch" to emptyList<String>()),
                mapOf("snack" to emptyList<String>()),
                mapOf("dinner" to emptyList<String>())
            ),
            "biometricEnabled" to false
        )

        db.collection("User")
            .add(user)
            .addOnSuccessListener { documentReference ->
                clearUserData()
                userSignInData.UserUID = current_user!!.uid
                userSignInData.UserDocId = documentReference.id
                Log.d("USERDOCID", documentReference.id)
            }
            .addOnFailureListener { exception ->
                Log.w("USERDOCID", "Error adding Document", exception)
            }
    }

    private fun clearUserData() {
        userData.firstName = ""
        userData.lastName = ""
        userData.email = ""
        userData.password = ""
        userData.username = ""
        userData.description = ""
        userData.profileimage = ""
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

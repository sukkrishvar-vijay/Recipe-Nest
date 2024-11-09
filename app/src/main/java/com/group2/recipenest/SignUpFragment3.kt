/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.group2.recipenest.databinding.CreateAccount3Binding

class SignUpFragment3: Fragment() {
    private var _binding: CreateAccount3Binding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var current_user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = CreateAccount3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.previousButton.setOnClickListener{
            loadFragment(SignUpFragment2())
        }

        binding.submitButton.setOnClickListener {

            val email = userData.email
            val password = userData.password

            //https://firebase.google.com/docs/auth/android/password-auth
            //https://stackoverflow.com/questions/65604918/createuserwithemailandpassword-addoncompletelistener-wont-work
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, navigate to main app screen
                        current_user = auth.currentUser
                        Toast.makeText(requireContext(), "Success!", Toast.LENGTH_SHORT).show()
                        // Navigate to your home activity or main screen
                        writeUserToFirebase()
                        userSignInData.UserUID = current_user!!.uid
                        loadFragment(AccountCreatedFragment())
                        //(activity as MainActivity).loadHomePage()
                    }
                    else {
                        // If sign-in fails, display a message to the user.
                        Toast.makeText(requireContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    // Method to write user sign up details to User collection in firebase
    private fun writeUserToFirebase(){
        val user = hashMapOf(
            "firstName" to userData.firstName,
            "lastName" to userData.lastName,
            "email" to userData.email,
            "username" to userData.username,
            "bio" to userData.description,
            "profileImageUrl" to userData.profileimage,
            "userUID" to current_user!!.uid,
            "favoriteCollection" to listOf(
                mapOf("breakfast" to emptyList<String>()),
                mapOf("lunch" to emptyList<String>()),
                mapOf("snack" to emptyList<String>()),
                mapOf("dinner" to emptyList<String>()),
            ),
            "biometricEnabled" to false
            )

        //https://firebase.google.com/docs/firestore/manage-data/add-data
        db.collection("User")
            .add(user)
            .addOnSuccessListener{documentReference ->
                clearUserData()
                userSignInData.UserUID = current_user!!.uid
                userSignInData.UserDocId = documentReference.id
                Log.d("USERDOCID",documentReference.id)
            }
            .addOnFailureListener { exception ->
                Log.w("USERDOCID", "Error adding Document", exception)
            }
    }

    //clearing the data class
    private fun clearUserData() {
        userData.firstName = ""
        userData.lastName = ""
        userData.email = ""
        userData.password = ""
        userData.username = ""
        userData.description = ""
        userData.profileimage = ""
    }

    //https://medium.com/@Max_Sir/mastering-android-fragments-managers-transactions-and-best-practices-in-kotlin-af00cb9b44ac
    //https://developer.android.com/guide/fragments/fragmentmanager
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
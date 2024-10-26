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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.SignInPageBinding
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    private var _binding: SignInPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignInPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        (activity as MainActivity).hideBottomNavigation()
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Handle sign-in button click
        binding.signInButton.setOnClickListener {
            val email = binding.emailtextField.editText?.text.toString().trim()
            val password = binding.passwordtextField.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                signInUser(email, password)
            }
        }

        binding.createAccountButton.setOnClickListener {
            // Navigate to the create account screen
            loadFragment(SignUpFragment())
        }
        
        binding.forgotPasswordButton.setOnClickListener {
            // Navigate to the forgot password screen
            loadFragment(ForgotPasswordFragment())
        }
    }

    // Sign in user with Firebase Authentication
    //https://firebase.google.com/docs/auth/android/password-auth
    //https://pillar-soft.com/blogs/Firebase_Auth_Android/
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to home page
                    Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                    val current_user = auth.currentUser
                    if (current_user != null) {
                        userSignInData.UserUID = current_user.uid
                    }
                    (activity as MainActivity).hideToolbar()
                    (activity as MainActivity).loadHomePage()
                } else {
                    // Sign in failed, show error
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
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

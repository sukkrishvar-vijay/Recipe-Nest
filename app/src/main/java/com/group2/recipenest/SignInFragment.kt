package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        // Inflate the layout for this fragment using ViewBinding
        _binding = SignInPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        (activity as MainActivity).hideBottomNavigation()

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

        // Handle "Create Account" button click
        binding.createAccountButton.setOnClickListener {
            // Navigate to the create account screen
            // You can use FragmentManager to load the create account fragment if you have it
        }

        // Handle "Forgot Password" button click
        binding.forgotPasswordButton.setOnClickListener {
            // Navigate to the forgot password screen or implement reset logic here
        }
    }

    // Sign in user with Firebase Authentication
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to home page
                    Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).loadHomePage()
                } else {
                    // Sign in failed, show error
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

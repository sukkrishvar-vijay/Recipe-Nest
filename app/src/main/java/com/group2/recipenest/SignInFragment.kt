package com.group2.recipenest

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.group2.recipenest.databinding.SignInPageBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class SignInFragment : Fragment() {

    private var _binding: SignInPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor
    private val LOCATION_PERMISSION_REQUEST_CODE = 101

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

        // Initialize Executor and BiometricPrompt
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                signInUserViaBio()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        // Setup PromptInfo for BiometricPrompt
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Sign In")
            .setSubtitle("Sign in using your fingerprint scan")
            .setNegativeButtonText("Cancel")
            .build()

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

        binding.iconFingerprintButton.setOnClickListener {
            if (isBiometricEnabled()) {
                checkBiometricAvailabilityAndAuthenticate()
            }
            else {
                Toast.makeText(requireContext(), "Biometric Login not enabled", Toast.LENGTH_SHORT).show()
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

        if (userSignInData.ShowAuthFirstTime && isBiometricEnabled()) {
            userSignInData.ShowAuthFirstTime = false
            checkBiometricAvailabilityAndAuthenticate()
        }
    }

    private fun isBiometricEnabled(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("UserSettings", MODE_PRIVATE)
        return sharedPreferences.getBoolean("biometricEnabled", false)
    }


    // Function to check biometric availability and start authentication
    private fun checkBiometricAvailabilityAndAuthenticate() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device supports biometric authentication
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(requireContext(), "No biometric hardware available", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(requireContext(), "Biometric hardware currently unavailable", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(requireContext(), "No biometric credentials enrolled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Sign in user with Firebase Authentication
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, request location permission
                    Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                    saveCredentials(email, password)
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        userSignInData.UserUID = currentUser.uid
                    }
                    (activity as MainActivity).hideToolbar()

                    // Check and request location permission
                    checkLocationPermission()
                } else {
                    // Sign in failed, show error
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @Suppress("DEPRECATION")
    private fun signInUserViaBio(){
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "UserCredentials",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val email = encryptedSharedPreferences.getString("email", null)
        val password = encryptedSharedPreferences.getString("password", null)

        if (email != null && password != null) {
            signInUser(email, password)
        }
        else {
            Toast.makeText(requireContext(), "No saved credentials", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveCredentials(email: String, password: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "UserCredentials",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        encryptedSharedPreferences.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    // Check if location permission is granted
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, load home page
            loadHomePage()
        } else {
            // Request location permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load home page
                loadHomePage()
            } else {
                // Permission denied, show toast and load home page without location functionality
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                loadHomePage()
            }
        }
    }

    private fun loadHomePage() {
        // Navigate to home page fragment
        (activity as MainActivity).loadHomePage()
    }

    // Load another fragment
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

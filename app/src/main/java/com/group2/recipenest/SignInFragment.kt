/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

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

        auth = FirebaseAuth.getInstance()
        (activity as MainActivity).hideBottomNavigation()
        (activity as AppCompatActivity).supportActionBar?.hide()

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

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Sign In")
            .setSubtitle("Sign in using your fingerprint scan")
            .setNegativeButtonText("Cancel")
            .build()

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
            loadFragment(SignUpFragment())
        }

        binding.forgotPasswordButton.setOnClickListener {
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


    private fun checkBiometricAvailabilityAndAuthenticate() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
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

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                    saveCredentials(email, password)
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        userSignInData.UserUID = currentUser.uid
                    }
                    (activity as MainActivity).hideToolbar()

                    checkLocationPermission()
                } else {
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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadHomePage()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadHomePage()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                loadHomePage()
            }
        }
    }

    private fun loadHomePage() {
        (activity as MainActivity).loadHomePage()
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

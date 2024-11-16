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
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.group2.recipenest.databinding.PasswordResetBinding

class ForgotPasswordFragment: Fragment() {
    private var _binding: PasswordResetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PasswordResetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(SignInFragment())
        }

        //https://firebase.google.com/docs/auth/android/manage-users
        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailtextField.editText?.text.toString().trim()
            if(email != ""){
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("PasswordReset", "Email sent.")
                            loadFragment(SignInFragment())
                        } else {
                            Log.e("PasswordReset", "Error: ${task.exception?.message}")
                        }
                    }

            }
            else{
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
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
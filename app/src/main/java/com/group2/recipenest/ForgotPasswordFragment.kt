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
        // Inflate the layout for this fragment using ViewBinding
        _binding = PasswordResetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            loadFragment(SignInFragment())
        }

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailtextField.editText?.text.toString().trim()
            //(activity as MainActivity).signInAccount()
            if(email != ""){
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Password reset email sent successfully
                            Log.d("PasswordReset", "Email sent.")
                            loadFragment(SignInFragment())
                        } else {
                            // Failed to send reset email
                            Log.e("PasswordReset", "Error: ${task.exception?.message}")
                        }
                    }

            }
            else{
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            }

        }

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
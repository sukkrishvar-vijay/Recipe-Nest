/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
 */

package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.backButton.setOnClickListener {
            loadFragment(SignInFragment())
        }

        //https://firebase.google.com/docs/auth/android/manage-users
        //https://stackoverflow.com/questions/42800349/forgot-password-in-firebase-for-android
        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailtextField.editText?.text.toString().trim()
            if(email != ""){
                binding.emailtextField.error = null
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadFragment(SignInFragment())
                        }
                    }

            }
            else{
                binding.emailtextField.error = "Email is required"
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
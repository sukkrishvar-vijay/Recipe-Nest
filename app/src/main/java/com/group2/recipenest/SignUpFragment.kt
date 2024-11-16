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
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.CreateAccount1Binding

class SignUpFragment:Fragment() {
    private var _binding: CreateAccount1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateAccount1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userData.firstName != ""){
            binding.firstNametextField.editText?.setText(userData.firstName)
            binding.lastNametextField.editText?.setText(userData.lastName)
            binding.emailtextField.editText?.setText(userData.email)
            binding.enterPasswordtextField.editText?.setText(userData.password)
            binding.confirmPasswordtextField.editText?.setText(userData.password)
        }

        binding.previousPageButton.setOnClickListener{
            loadFragment(SignInFragment())
        }

        binding.nextPageButton.setOnClickListener {
            val firstname = binding.firstNametextField.editText?.text.toString().trim()
            val lastname = binding.lastNametextField.editText?.text.toString().trim()
            val email = binding.emailtextField.editText?.text.toString().trim()
            val password = binding.enterPasswordtextField.editText?.text.toString().trim()
            val confirm_password = binding.confirmPasswordtextField.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || confirm_password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all text fields", Toast.LENGTH_SHORT).show()
            }
            else if(password!=confirm_password) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                userData.firstName = firstname
                userData.lastName = lastname
                userData.email = email
                userData.password = password
                loadFragment(SignUpFragment2())
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
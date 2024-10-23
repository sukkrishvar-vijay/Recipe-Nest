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
        // Inflate the layout for this fragment using ViewBinding
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
            //(activity as MainActivity).signInAccount()
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
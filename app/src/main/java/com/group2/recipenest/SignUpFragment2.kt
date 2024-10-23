package com.group2.recipenest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.CreateAccount2Binding

class SignUpFragment2: Fragment() {
    private var _binding: CreateAccount2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = CreateAccount2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userData.username != "" || userData.description != ""){
            binding.usernametextField.editText?.setText(userData.username)
            binding.userDescriptiontextField.editText?.setText(userData.description)
        }

        binding.previousPageButton.setOnClickListener{
            val username = binding.usernametextField.editText?.text.toString().trim()
            val userdescription = binding.userDescriptiontextField.editText?.text.toString().trim()

            if(username.isNotEmpty()){
                userData.username = username
                userData.description = userdescription
            }
            loadFragment(SignUpFragment())
        }

        binding.nextPageButton.setOnClickListener {
            val username = binding.usernametextField.editText?.text.toString().trim()
            val userdescription = binding.userDescriptiontextField.editText?.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill the username text field", Toast.LENGTH_SHORT).show()
            }
            else{
                userData.username = username
                userData.description = userdescription

                loadFragment(SignUpFragment3())
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
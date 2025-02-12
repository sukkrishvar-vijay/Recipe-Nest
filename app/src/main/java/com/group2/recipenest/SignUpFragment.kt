/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
 */

package com.group2.recipenest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
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

        passwordFocusListener()

        binding.previousPageButton.setOnClickListener{
            clearUserData()
            loadFragment(SignInFragment())
        }

        binding.emailtext.setOnFocusChangeListener { _, focused ->
            if (focused){
                binding.emailtextField.error = null
            }
        }

        binding.firstNametext.setOnFocusChangeListener { _, focused ->
            if (focused){
                binding.firstNametextField.error = null
            }
        }

        binding.lastNametext.setOnFocusChangeListener { _, focused ->
            if (focused){
                binding.lastNametextField.error = null
            }
        }

        binding.confirmPasswordEditText.setOnFocusChangeListener { _, focused ->
            if (focused){
                binding.confirmPasswordtextField.error = null
            }
        }

        binding.confirmPasswordEditText.addTextChangedListener { text ->
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = text.toString()
            if (password != confirmPassword) {
                binding.confirmPasswordtextField.helperText= "Password do not match"
                binding.confirmPasswordtextField.setHelperTextColor(ColorStateList.valueOf(Color.RED))
            } else {
                binding.confirmPasswordtextField.helperText = null
            }
        }

        binding.nextPageButton.setOnClickListener {
            val firstname = binding.firstNametextField.editText?.text.toString().trim()
            val lastname = binding.lastNametextField.editText?.text.toString().trim()
            val email = binding.emailtextField.editText?.text.toString().trim()
            val password = binding.enterPasswordtextField.editText?.text.toString().trim()
            val confirm_password = binding.confirmPasswordtextField.editText?.text.toString().trim()

            binding.firstNametextField.error = null
            binding.lastNametextField.error = null
            binding.emailtextField.error = null
            binding.enterPasswordtextField.error = null
            binding.confirmPasswordtextField.error = null

            if(password!=confirm_password) {
                binding.confirmPasswordtextField.error = "Passwords do not match"
            }

            if (email.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || confirm_password.isEmpty()) {
                if (email.isEmpty()) {
                    binding.emailtextField.error = "Email is required"
                } else {
                    binding.emailtextField.error = null
                }
                if (password.isEmpty()) {
                    binding.enterPasswordtextField.error = "Password is required"
                }
                if (confirm_password.isEmpty()) {
                    binding.confirmPasswordtextField.error = "Password re-entry is required"
                }
                if (firstname.isEmpty()){
                    binding.firstNametextField.error = "First Name is required"
                }
                if (lastname.isEmpty()){
                    binding.lastNametextField.error = "Last Name is required"
                }
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if(email.isNotEmpty()){
                    binding.emailtextField.error = "Invalid email address"
                }
            }

            if(firstname.isNotEmpty() && lastname.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password == confirm_password && email.isNotEmpty() && password.isNotEmpty() && validPassword() == null) {
                userData.firstName = firstname
                userData.lastName = lastname
                userData.email = email
                userData.password = password
                loadFragment(SignUpFragment2())
            }
            else{
                binding.enterPasswordtextField.error = validPassword()
            }

        }
    }

    //https://www.youtube.com/watch?v=Gc0sLf91QeM&list=PL8Bp2Wfez8Pe3sTzSwBnf0ZbuBftF-Ehd&index=9
    private fun passwordFocusListener() {
        binding.passwordEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.enterPasswordtextField.helperText = null
                binding.enterPasswordtextField.error = validPassword()
            }
            else{
                binding.enterPasswordtextField.helperText = "Minimum 8 characters, must contain A-Z, a-z, 0-9, !@#$%^&*."
                binding.enterPasswordtextField.setHelperTextColor(ColorStateList.valueOf(Color.BLACK))
            }
        }
    }

    private fun validPassword(): String? {
        val passwordText = binding.passwordEditText.text.toString()
        if(passwordText.isEmpty()) {
            return "Password is required"
        }
        if(passwordText.length < 8) {
            return "Minimum 8 character password"
        }
        if(!passwordText.matches(".*[A-Z].*".toRegex())) {
            return "Must contain 1 uppercase character"
        }
        if(!passwordText.matches(".*[a-z].*".toRegex())) {
            return "Must contain 1 lowercase character"
        }
        if (!passwordText.matches(".*\\d.*".toRegex())) {
            return "Must contain at least 1 numeric character"
        }
        if(!passwordText.matches(".*[@#\$%^&+=].*".toRegex())) {
            return "Must contain 1 special character (@#\$%^&+=)"
        }
        return null
    }

    //clearing the data class
    private fun clearUserData() {
        userData.firstName = ""
        userData.lastName = ""
        userData.email = ""
        userData.password = ""
        userData.username = ""
        userData.description = ""
        userData.profileimage = ""
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
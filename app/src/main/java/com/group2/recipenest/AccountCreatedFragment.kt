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
import androidx.fragment.app.Fragment
import com.group2.recipenest.databinding.AccountCreatedBinding

//Fragment to greet user and take them to home page
class AccountCreatedFragment:Fragment() {
    private var _binding: AccountCreatedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AccountCreatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Takes the user to Home Page after creating the account
        binding.startExploringButton.setOnClickListener {
            (activity as MainActivity).loadHomePage()
        }

    }

}
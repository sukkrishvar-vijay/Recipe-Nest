/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 * https://openai.com/index/chatgpt/
 * https://gemini.google.com/app
 */

package com.group2.recipenest

import RecipeCardModel
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class SearchFragment : Fragment() {

    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var adapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore

    private lateinit var difficultyLevelChip: Chip
    private lateinit var cookingTimeChip: Chip
    private lateinit var cuisineTypeChip: Chip
    private lateinit var clearFiltersButton: MaterialButton

    private var selectedDifficultyLevel: String? = null
    private var selectedCookingTime: String? = null
    private val selectedCuisineTypes = mutableSetOf<String>()

    private lateinit var searchResultCount: TextView
    private lateinit var noSearchResult: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        firestore = FirebaseFirestore.getInstance()

        // RecyclerView setup and Adapter configuration based on Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-string.html
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        searchResultsRecyclerView.adapter = adapter

        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val searchIcon = view.findViewById<ImageView>(R.id.searchIcon)

        searchIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchRecipes(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a recipe to search", Toast.LENGTH_SHORT).show()
            }
        }

        // TextWatcher for monitoring text input based on Android developer documentation
        // https://developer.android.com/reference/android/text/TextWatcher
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/empty-list.html
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adapter.updateRecipes(emptyList())
                    searchResultCount.text = ""
                    searchResultCount.visibility = View.GONE
                    noSearchResult.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Chip components for filter selection based on Material Design documentation
        // https://material.io/components/chips
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
        difficultyLevelChip = view.findViewById(R.id.difficultyLevelChip)
        cookingTimeChip = view.findViewById(R.id.cookingTimeChip)
        cuisineTypeChip = view.findViewById(R.id.cuisineTypeChip)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)
        searchResultCount = view.findViewById(R.id.resultsCountTextView)
        noSearchResult = view.findViewById(R.id.noResultsTextView)

        difficultyLevelChip.setOnClickListener { showDifficultyLevelDialog(difficultyLevelChip) }
        cookingTimeChip.setOnClickListener { showCookingTimeDialog(cookingTimeChip) }
        cuisineTypeChip.setOnClickListener { showCuisineTypeDialog(cuisineTypeChip) }

        clearFiltersButton.setOnClickListener { clearAllFilters() }

        return view
    }

    // Firestore data retrieval and filtering based on Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
    private fun fetchRecipes(query: String) {
        val searchQuery = query.lowercase()
        var firestoreQuery: Query = firestore.collection("Recipes")

        selectedDifficultyLevel?.let {
            firestoreQuery = firestoreQuery.whereEqualTo("difficultyLevel", it)
        }

        firestoreQuery.get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()

                for (document in documents) {
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<*>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    val recipeUserId = document.getString("recipeUserId") ?: ""
                    val dateRecipeAdded = document.getDate("dateRecipeAdded") ?: Date()
                    val recipeImageUrl = document.getString("recipeImageUrl") ?: ""
                    val recipeId = document.id

                    val isCookingTimeMatch = selectedCookingTime?.let { cookingTimeText ->
                        val cookingTimeInMinutes = cookingTimeText.split(" ")[0].toIntOrNull()
                        cookingTimeInMinutes?.let { cookingTime == it } ?: true
                    } ?: true

                    val isTitleMatch = recipeTitle.lowercase().contains(searchQuery)

                    val isCuisineTypeMatch = selectedCuisineTypes.all { it in (cuisineTypeList ?: emptyList<String>()) }

                    if (isCookingTimeMatch && isTitleMatch && isCuisineTypeMatch) {
                        val recipe = RecipeCardModel(
                            recipeUserId = recipeUserId,
                            recipeDescription = recipeDescription,
                            recipeTitle = recipeTitle,
                            cookingTime = cookingTime,
                            avgRating = avgRating.toDouble(),
                            recipeImageUrl = recipeImageUrl,
                            difficultyLevel = difficultyLevel,
                            cuisineType = cuisineType,
                            recipeId = recipeId,
                            dateRecipeAdded = dateRecipeAdded
                        )
                        recipeList.add(recipe)
                    }
                }
                adapter.updateRecipes(recipeList)
                if (recipeList.isNotEmpty()){
                    searchResultCount.setText("${recipeList.count()} recipe(s) found")
                    searchResultCount.visibility = View.VISIBLE
                    noSearchResult.visibility = View.GONE
                }else{
                    noSearchResult.visibility = View.VISIBLE
                    noSearchResult.text = "No recipes found!"
                    searchResultCount.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching recipes. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    // AlertDialog setup for filter selection based on Android developer documentation
    // https://developer.android.com/reference/android/app/AlertDialog
    // https://m2.material.io/components/chips/android
    private fun showDifficultyLevelDialog(difficultyLevelChip: Chip) {
        val options = arrayOf("Easy", "Medium", "Hard")
        var selectedOption: String? = null

        // Single-choice AlertDialog setup based on Android developer documentation
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setSingleChoiceItems(java.lang.CharSequence[],%20int,%20android.content.DialogInterface.OnClickListener)
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/index-of.html
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomRadioButton)
        builder.setTitle("Difficulty Level")
        builder.setSingleChoiceItems(options, options.indexOf(selectedDifficultyLevel)) { _, which ->
            selectedOption = options[which]
        }
        builder.setPositiveButton("OK") { _, _ ->
            if (selectedOption != null) {
                selectedDifficultyLevel = selectedOption
                difficultyLevelChip.isChecked = true
                difficultyLevelChip.text = selectedDifficultyLevel
            } else {
                difficultyLevelChip.isChecked = false
                difficultyLevelChip.text = "Difficulty Level"
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            selectedDifficultyLevel = null
            difficultyLevelChip.isChecked = false
            difficultyLevelChip.text = "Difficulty Level"
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
        dialog.show()
    }

    // AlertDialog setup for filter selection based on Android developer documentation
    // https://developer.android.com/reference/android/app/AlertDialog
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/index-of.html
    private fun showCookingTimeDialog(cookingTimeChip: Chip) {
        val options = arrayOf("15 mins", "30 mins", "45 mins", "60 mins")
        var selectedOption: String? = null

        // Single-choice AlertDialog setup based on Android developer documentation
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setSingleChoiceItems(java.lang.CharSequence[],%20int,%20android.content.DialogInterface.OnClickListener)
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomRadioButton)
        builder.setTitle("Cooking Time")
        builder.setSingleChoiceItems(options, options.indexOf(selectedCookingTime)) { _, which ->
            selectedOption = options[which]
        }
        builder.setPositiveButton("OK") { _, _ ->
            if (selectedOption != null) {
                selectedCookingTime = selectedOption
                cookingTimeChip.isChecked = true
                cookingTimeChip.text = selectedCookingTime
            } else {
                cookingTimeChip.isChecked = false
                cookingTimeChip.text = "Cooking Time"
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            selectedCookingTime = null
            cookingTimeChip.isChecked = false
            cookingTimeChip.text = "Cooking Time"
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
        dialog.show()
    }

    // AlertDialog setup for filter selection based on Android developer documentation
    // https://developer.android.com/reference/android/app/AlertDialog
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean-array/
    private fun showCuisineTypeDialog(cuisineTypeChip: Chip) {
        val options = arrayOf("Vegetarian", "Non-Vegetarian", "Chinese", "Thai", "American", "Indian")
        val checkedItems = BooleanArray(options.size) { selectedCuisineTypes.contains(options[it]) }

        // Multi-choice AlertDialog implementation based on Android developer documentation
        // https://developer.android.com/reference/android/app/AlertDialog.Builder#setMultiChoiceItems(java.lang.CharSequence[],%20boolean[],%20android.content.DialogInterface.OnMultiChoiceClickListener)
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomRadioButton)
        builder.setTitle("Cuisine Types")
        builder.setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedCuisineTypes.add(options[which])
            } else {
                selectedCuisineTypes.remove(options[which])
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            if (selectedCuisineTypes.isNotEmpty()) {
                cuisineTypeChip.isChecked = true
                cuisineTypeChip.text = selectedCuisineTypes.joinToString(", ")
            } else {
                cuisineTypeChip.isChecked = false
                cuisineTypeChip.text = "Cuisine Types"
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            selectedCuisineTypes.clear()
            cuisineTypeChip.isChecked = false
            cuisineTypeChip.text = "Cuisine Types"
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
        dialog.show()
    }

    private fun clearAllFilters() {
        selectedDifficultyLevel = null
        selectedCookingTime = null
        selectedCuisineTypes.clear()

        difficultyLevelChip.isChecked = false
        difficultyLevelChip.text = "Difficulty Level"

        cookingTimeChip.isChecked = false
        cookingTimeChip.text = "Cooking Time"

        cuisineTypeChip.isChecked = false
        cuisineTypeChip.text = "Cuisine Types"
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    // Passing data between fragments using Bundle based on Android developer documentation
    // https://developer.android.com/guide/fragments/communicate
    // https://developer.android.com/reference/kotlin/android/os/Bundle
    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val fragment = RecipeDetailsFragment()
        val bundle = Bundle().apply {
            putString("recipeTitle", recipe.recipeTitle)
            putString("recipeUserId", recipe.recipeUserId)
            putString("recipeDescription", recipe.recipeDescription)
            putString("difficultyLevel", recipe.difficultyLevel)
            putInt("cookingTime", recipe.cookingTime)
            putString("cuisineType", recipe.cuisineType)
            putString("recipeId", recipe.recipeId)
            putString("recipeImageUrl", recipe.recipeImageUrl)
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
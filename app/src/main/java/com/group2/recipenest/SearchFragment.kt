/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipeCardModel
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        firestore = FirebaseFirestore.getInstance()

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

        difficultyLevelChip = view.findViewById(R.id.difficultyLevelChip)
        cookingTimeChip = view.findViewById(R.id.cookingTimeChip)
        cuisineTypeChip = view.findViewById(R.id.cuisineTypeChip)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)

        difficultyLevelChip.setOnClickListener { showDifficultyLevelDialog(difficultyLevelChip) }
        cookingTimeChip.setOnClickListener { showCookingTimeDialog(cookingTimeChip) }
        cuisineTypeChip.setOnClickListener { showCuisineTypeDialog(cuisineTypeChip) }

        clearFiltersButton.setOnClickListener { clearAllFilters() }

        return view
    }

    private fun fetchRecipes(query: String) {
        val searchQuery = query.lowercase()

        firestore.collection("Recipes")
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()
                for (document in documents) {
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"

                    if (recipeTitle.lowercase().contains(searchQuery)) {
                        val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                        val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                        val difficultyLevel = document.getString("difficultyLevel") ?: ""
                        val cuisineTypeList = document.get("cuisineType") as? List<*>
                        val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                        val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                        val recipeUserId = document.getString("recipeUserId") ?: ""
                        val recipeId = document.id

                        val recipe = RecipeCardModel(
                            recipeUserId = recipeUserId,
                            recipeDescription = recipeDescription,
                            recipeTitle = recipeTitle,
                            cookingTime = cookingTime,
                            avgRating = avgRating,
                            imageResId = R.drawable.placeholder_recipe_image,
                            difficultyLevel = difficultyLevel,
                            cuisineType = cuisineType,
                            recipeId = recipeId
                        )
                        recipeList.add(recipe)
                    }
                }
                adapter.updateRecipes(recipeList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDifficultyLevelDialog(difficultyLevelChip: Chip) {
        val options = arrayOf("Easy", "Medium", "Hard")
        var selectedOption: String? = null

        val builder = AlertDialog.Builder(requireContext())
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
            difficultyLevelChip.isChecked = false
            difficultyLevelChip.text = "Difficulty Level"
        }
        builder.show()
    }

    private fun showCookingTimeDialog(cookingTimeChip: Chip) {
        val options = arrayOf("15 mins", "30 mins", "45 mins", "60 mins")
        var selectedOption: String? = null

        val builder = AlertDialog.Builder(requireContext())
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
            cookingTimeChip.isChecked = false
            cookingTimeChip.text = "Cooking Time"
        }
        builder.show()
    }

    private fun showCuisineTypeDialog(cuisineTypeChip: Chip) {
        val options = arrayOf("Vegetarian", "Non-Vegetarian", "Chinese", "Thai", "American", "Indian")
        val checkedItems = BooleanArray(options.size) { selectedCuisineTypes.contains(options[it]) }

        val builder = AlertDialog.Builder(requireContext())
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
            cuisineTypeChip.isChecked = false
            cuisineTypeChip.text = "Cuisine Types"
        }
        builder.show()
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
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
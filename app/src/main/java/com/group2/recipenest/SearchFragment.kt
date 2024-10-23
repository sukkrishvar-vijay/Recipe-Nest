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

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize with an empty list and set click handling
        adapter = RecipeCardsAdapter(listOf()) { recipe ->
            navigateToRecipeDetailsFragment(recipe)
        }
        searchResultsRecyclerView.adapter = adapter

        // Handle Search Bar
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val searchIcon = view.findViewById<ImageView>(R.id.searchIcon)

        // Trigger search when search icon is pressed
        searchIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchRecipes(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a recipe to search", Toast.LENGTH_SHORT).show()
            }
        }

        // Access the chips and clear button
        difficultyLevelChip = view.findViewById(R.id.difficultyLevelChip)
        cookingTimeChip = view.findViewById(R.id.cookingTimeChip)
        cuisineTypeChip = view.findViewById(R.id.cuisineTypeChip)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)

        // Set click listeners for chips to show dialog
        difficultyLevelChip.setOnClickListener { showDifficultyLevelDialog(difficultyLevelChip) }
        cookingTimeChip.setOnClickListener { showCookingTimeDialog(cookingTimeChip) }
        cuisineTypeChip.setOnClickListener { showCuisineTypeDialog(cuisineTypeChip) }

        // Handle Clear Filters Button
        clearFiltersButton.setOnClickListener { clearAllFilters() }

        return view
    }

    // Fetch recipes from Firestore based on search query
    private fun fetchRecipes(query: String) {
        firestore.collection("Recipes")
            .whereGreaterThanOrEqualTo("recipeTitle", query)
            .whereLessThanOrEqualTo("recipeTitle", query + '\uf8ff')
            .get()
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
                adapter.updateRecipes(recipeList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Show difficulty level dialog
    private fun showDifficultyLevelDialog(difficultyLevelChip: Chip) {
        val options = arrayOf("Easy", "Medium", "Hard")
        var selectedOption = selectedDifficultyLevel

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Difficulty Level")
        builder.setSingleChoiceItems(options, options.indexOf(selectedOption)) { _, which ->
            selectedOption = options[which]
        }
        builder.setPositiveButton("OK") { _, _ ->
            selectedDifficultyLevel = selectedOption
            difficultyLevelChip.isCheckable = true
            difficultyLevelChip.isChecked = true
            difficultyLevelChip.text = selectedDifficultyLevel // Update chip text
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Show cooking time dialog
    private fun showCookingTimeDialog(cookingTimeChip: Chip) {
        val options = arrayOf("15 mins", "30 mins", "45 mins", "60 mins")
        var selectedOption = selectedCookingTime

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Cooking Time")
        builder.setSingleChoiceItems(options, options.indexOf(selectedOption)) { _, which ->
            selectedOption = options[which]
        }
        builder.setPositiveButton("OK") { _, _ ->
            selectedCookingTime = selectedOption
            cookingTimeChip.isCheckable = true
            cookingTimeChip.isChecked = true
            cookingTimeChip.text = selectedCookingTime // Update chip text
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Show cuisine type dialog
    private fun showCuisineTypeDialog(cuisineTypeChip: Chip) {
        val options = arrayOf("Vegetarian", "Non-Vegetarian", "Chinese", "Thai", "American", "Indian")
        val checkedItems = BooleanArray(options.size) { selectedCuisineTypes.contains(options[it]) }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Cuisine Types")
        builder.setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedCuisineTypes.add(options[which])
            } else {
                selectedCuisineTypes.remove(options[which])
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            cuisineTypeChip.isCheckable = true
            cuisineTypeChip.isChecked = selectedCuisineTypes.isNotEmpty()
            cuisineTypeChip.text = if (selectedCuisineTypes.isEmpty()) {
                "Cuisine Type"
            } else {
                selectedCuisineTypes.joinToString(", ") // Update chip text
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Function to clear all filter selections
    private fun clearAllFilters() {
        selectedDifficultyLevel = null
        selectedCookingTime = null
        selectedCuisineTypes.clear()

        // Reset the chips to their default state
        difficultyLevelChip.isChecked = false
        difficultyLevelChip.text = "Difficulty Level"

        cookingTimeChip.isChecked = false
        cookingTimeChip.text = "Cooking Time"

        cuisineTypeChip.isChecked = false
        cuisineTypeChip.text = "Cuisine Type"
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    // Navigate to RecipeDetailsFragment
    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val fragment = RecipeDetailsFragment()

        // Pass recipe details to the fragment via arguments
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

        // Navigate to RecipeDetailsFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

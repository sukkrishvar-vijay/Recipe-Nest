package com.group2.recipenest

import RecipeCardModel
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Tasks

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
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    adapter.updateRecipes(emptyList()) // Clear the recipe results
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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

    private fun fetchRecipes(query: String) {
        val searchQuery = query.lowercase()
        var firestoreQuery: Query = firestore.collection("Recipes")

        // Log the filters being applied to help with debugging
        Log.d("SearchFragment", "Filters - Difficulty: $selectedDifficultyLevel, Cooking Time: $selectedCookingTime, Cuisine Types: $selectedCuisineTypes")

        // Apply Difficulty Level Filter (if selected) directly in the Firestore query
        selectedDifficultyLevel?.let {
            firestoreQuery = firestoreQuery.whereEqualTo("difficultyLevel", it)
        }

        // We don't apply the cuisine type filter in Firestore if there are multiple selections,
        // because we need to ensure all selected types are present in each recipe.

        // Execute the query to fetch documents based on difficulty and other conditions
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
                    val recipeId = document.id

                    // Check if cooking time matches the filter (in-memory filtering)
                    val isCookingTimeMatch = selectedCookingTime?.let { cookingTimeText ->
                        val cookingTimeInMinutes = cookingTimeText.split(" ")[0].toIntOrNull()
                        cookingTimeInMinutes?.let { cookingTime == it } ?: true
                    } ?: true

                    // Check if title matches the search query
                    val isTitleMatch = recipeTitle.lowercase().contains(searchQuery)

                    // Check if the recipe contains all selected cuisine types
                    val isCuisineTypeMatch = selectedCuisineTypes.all { it in (cuisineTypeList ?: emptyList<String>()) }

                    // If all conditions match, add the recipe to the list
                    if (isCookingTimeMatch && isTitleMatch && isCuisineTypeMatch) {
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

                // Update the adapter with the filtered recipes
                adapter.updateRecipes(recipeList)

                // Show toast only if the recipe list is empty
                if (recipeList.isEmpty()) {
                    Toast.makeText(requireContext(), "No recipes found with your search", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching recipes. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }




    // Show difficulty level dialog
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
            selectedDifficultyLevel = null // Reset the difficulty level filter
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
            selectedCookingTime = null // Reset the cooking time filter
            cookingTimeChip.isChecked = false
            cookingTimeChip.text = "Cooking Time"
        }
        builder.show()
    }


    // Show cuisine type dialog
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
            selectedCuisineTypes.clear() // Clear cuisine types filter
            cuisineTypeChip.isChecked = false
            cuisineTypeChip.text = "Cuisine Types"
        }
        builder.show()
    }


    // Clear all filter selections
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

    // Navigate to RecipeDetailsFragment
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
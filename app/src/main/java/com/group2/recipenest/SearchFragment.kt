package com.group2.recipenest

import RecipeCardModel
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
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var adapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore

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

        // Initialize with an empty list
        adapter = RecipeCardsAdapter(listOf()) { recipe ->
            // Handle recipe card click
            Toast.makeText(requireContext(), "Clicked on: ${recipe.recipeTitle}", Toast.LENGTH_SHORT).show()
            // You can navigate to a detailed recipe screen if required here
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
                    // Safely retrieve each field from Firestore document
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<*>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    val recipeUserId = document.getString("recipeUserId") ?: ""
                    val recipeId = document.id

                    // Create a RecipeCardModel object and add it to the list
                    val recipe = RecipeCardModel(
                        recipeUserId = recipeUserId,
                        recipeDescription = recipeDescription,
                        recipeTitle = recipeTitle,
                        cookingTime = cookingTime,
                        avgRating = avgRating,
                        imageResId = R.drawable.placeholder_recipe_image,
                        difficultyLevel = difficultyLevel,
                        cuisineType = cuisineType,
                        recipeId = recipeId  // Pass the recipeId here
                    )
                    recipeList.add(recipe)
                }
                // Update the adapter with the fetched recipes
                adapter.updateRecipes(recipeList)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Toast.makeText(requireContext(), "Failed to fetch recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onResume() {
        super.onResume()
        // Hide the AppBar if it's visible
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the AppBar again when leaving the fragment
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
}

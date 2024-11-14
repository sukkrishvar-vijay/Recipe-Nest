package com.group2.recipenest

import RecipeCardModel
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.Date

class MyRecipesFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsWithLongClickAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_recipes_collection, container, false)

        firestore = Firebase.firestore
        storage = Firebase.storage  // Initialize Firebase Storage here

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "My Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_new_recipe)
        fab.setOnClickListener {
            val addRecipeFragment = AddRecipeFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addRecipeFragment)
                .addToBackStack(null)
                .commit()
        }

        recipeRecyclerView = view.findViewById(R.id.my_recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserRecipes()
    }

    private fun fetchUserRecipes() {
        firestore.collection("Recipes")
            .whereEqualTo("recipeUserId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val recipeList = mutableListOf<RecipeCardModel>()

                for (document in documents) {
                    val recipeTitle = document.getString("recipeTitle") ?: "Untitled"
                    val cookingTime = document.getLong("cookingTime")?.toInt() ?: 0
                    val avgRating = document.getDouble("avgRating")?.toString() ?: "N/A"
                    val difficultyLevel = document.getString("difficultyLevel") ?: ""
                    val cuisineTypeList = document.get("cuisineType") as? List<String>
                    val cuisineType = cuisineTypeList?.joinToString(", ") ?: "Unknown"
                    val recipeDescription = document.getString("recipeDescription") ?: "N/A"
                    val recipeUserId = document.getString("recipeUserId") ?: ""
                    val dateRecipeAdded = document.getDate("dateRecipeAdded") ?: Date()
                    val recipeImageUrl = document.getString("recipeImageUrl") ?: ""
                    val recipeId = document.id

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

                recipeAdapter = RecipeCardsWithLongClickAdapter(
                    recipeList,
                    onClick = { recipe -> navigateToRecipeDetailsFragment(recipe) },
                    onLongClick = { recipe -> showEditDeleteDialog(recipe) }
                )
                recipeRecyclerView.adapter = recipeAdapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun showEditDeleteDialog(recipe: RecipeCardModel) {
        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> editRecipe(recipe)
                1 -> deleteRecipe(recipe)
            }
        }
        builder.show()
    }

    private fun editRecipe(recipe: RecipeCardModel) {
        val addRecipeFragment = AddRecipeFragment()
        val bundle = Bundle().apply {
            putString("recipeUserId", recipe.recipeUserId)
            putString("recipeId", recipe.recipeId)
            putString("recipeTitle", recipe.recipeTitle)
            putString("recipeDescription", recipe.recipeDescription)
            putString("difficultyLevel", recipe.difficultyLevel)
            putInt("cookingTime", recipe.cookingTime)
            putString("cuisineType", recipe.cuisineType)
            putString("recipeImageUrl", recipe.recipeImageUrl)
            putDouble("avgRating", recipe.avgRating)
        }
        addRecipeFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addRecipeFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteRecipe(recipe: RecipeCardModel) {
        // Show confirmation dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Proceed with deletion if user confirms
                firestore.collection("Recipes").document(recipe.recipeId)
                    .delete()
                    .addOnSuccessListener {
                        recipe.recipeImageUrl.takeIf { it!!.isNotEmpty() }?.let { imageUrl ->
                            val storageRef = storage.getReferenceFromUrl(imageUrl)
                            storageRef.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Recipe deleted successfully!", Toast.LENGTH_SHORT).show()
                                    fetchUserRecipes() // Refreshes the list after deletion
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show()
                                }
                        } ?: run {
                            Toast.makeText(requireContext(), "Recipe deleted successfully!", Toast.LENGTH_SHORT).show()
                            fetchUserRecipes()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete recipe.", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss() // Close the dialog
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Close the dialog without deleting
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        dialog.show()
    }



    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val recipeDetailsFragment = RecipeDetailsFragment()
        val bundle = Bundle().apply {
            putString("recipeUserId", recipe.recipeUserId)
            putString("recipeDescription", recipe.recipeDescription)
            putString("recipeTitle", recipe.recipeTitle)
            putString("avgRating", recipe.avgRating.toString())
            putString("difficultyLevel", recipe.difficultyLevel)
            putInt("cookingTime", recipe.cookingTime)
            putString("cuisineType", recipe.cuisineType)
            putString("recipeId", recipe.recipeId)
            putString("recipeImageUrl", recipe.recipeImageUrl)
        }

        recipeDetailsFragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}

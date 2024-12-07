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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private lateinit var emptyMessageText: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_recipes_collection, container, false)

        firestore = Firebase.firestore
        storage = Firebase.storage

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
        emptyMessageText = view.findViewById(R.id.empty_message_text)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserRecipes()
    }
    // Fetches recipes created by the user from Firestore
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-list/
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

                if (recipeList.isEmpty()) {
                    recipeRecyclerView.visibility = View.GONE
                    emptyMessageText.visibility = View.VISIBLE
                } else {
                    recipeRecyclerView.visibility = View.VISIBLE
                    emptyMessageText.visibility = View.GONE

                    val sortedList = recipeList.sortedByDescending { it.dateRecipeAdded }
                    // RecyclerView adapter binding based on Android developer guide
                    // https://developer.android.com/guide/topics/ui/layout/recyclerview
                    recipeAdapter = RecipeCardsWithLongClickAdapter(
                        sortedList,
                        onClick = { recipe -> navigateToRecipeDetailsFragment(recipe) },
                        onLongClick = { recipe -> showEditDeleteDialog(recipe) }
                    )
                    recipeRecyclerView.adapter = recipeAdapter
                }

            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
    // Display a dialog with Edit and Delete options for the selected recipe
    // https://developer.android.com/develop/ui/views/components/dialogs
    // https://stackoverflow.com/questions/45972079/when-to-use-alertdialog-builder-settitle-vs-dialog-settitle
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
    // Navigates to AddRecipeFragment with pre-filled recipe details for editing
    // https://www.geeksforgeeks.org/bundle-in-android-with-example/
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
    // Show confirmation dialog before deleting the recipe
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/take-if.html
    // https://stackoverflow.com/questions/50174614/firebase-get-getreferencefromurl
    private fun deleteRecipe(recipe: RecipeCardModel) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                val recipeId = recipe.recipeId
                
                firestore.collection("Recipes").document(recipeId)
                    .delete()
                    .addOnSuccessListener {
                        removeRecipeFromAllFavorites(recipeId) { isRemoved ->
                            if (isRemoved) {
                                recipe.recipeImageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                                    val storageRef = storage.getReferenceFromUrl(imageUrl)
                                    storageRef.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Recipe deleted successfully!", Toast.LENGTH_SHORT).show()
                                            fetchUserRecipes()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(requireContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show()
                                        }
                                } ?: run {
                                    Toast.makeText(requireContext(), "Recipe deleted successfully!", Toast.LENGTH_SHORT).show()
                                    fetchUserRecipes()
                                }

                            } else {
                                Toast.makeText(requireContext(), "Failed to update favorites.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete recipe.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    private fun removeRecipeFromAllFavorites(recipeId: String, callback: (Boolean) -> Unit) {
        firestore.collection("User").get()
            .addOnSuccessListener { users ->
                val batch = firestore.batch()

                for (user in users) {
                    val userDocRef = user.reference
                    val favoriteCollection = user.get("favoriteCollection") as? MutableList<Map<String, MutableList<String>>>

                    favoriteCollection?.let { collection ->
                        val updatedCollection = collection.map { map ->
                            map.mapValues { (key, recipeIds) ->
                                recipeIds.filterNot { it == recipeId }.toMutableList()
                            }
                        }
                        batch.update(userDocRef, "favoriteCollection", updatedCollection)
                    }
                }

                batch.commit()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
    }


    // Navigates to the recipe details fragment with selected recipe data
    // https://www.geeksforgeeks.org/bundle-in-android-with-example/
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

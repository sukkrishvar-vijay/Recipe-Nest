/*
 * Some of the code blocks in this file have been developed with assistance from AI tools, which were used to help in various stages of the project,
 * including code generation, identifying bugs, and fixing errors related to app crashes. The AI provided guidance in modifying
 * and improving the structure of the code while adhering to Android development best practices. All generated solutions were reviewed
 * and tested for functionality before implementation.
 */

package com.group2.recipenest

import RecipeCardModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyRecipesFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter
    private lateinit var firestore: FirebaseFirestore

    private var currentUserId = userSignInData.UserDocId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_recipes_collection, container, false)

        firestore = Firebase.firestore

        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Toolbar setup and customization based on Android developer documentation
        // https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
        // https://developer.android.com/reference/android/content/res/Resources
        toolbar.title = "My Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // FloatingActionButton usage and fragment transaction learned from Android developer guide
        // https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton
       //  https://discuss.kotlinlang.org/t/findviewbyid-with-variable/26344
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_new_recipe)
        fab.setOnClickListener {
            val addRecipeFragment = AddRecipeFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addRecipeFragment)
                .addToBackStack(null)
                .commit()
        }

        // RecyclerView setup and LinearLayoutManager usage adapted from Android developer documentation
        // https://developer.android.com/guide/topics/ui/layout/recyclerview
        recipeRecyclerView = view.findViewById(R.id.my_recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserRecipes()
    }

    // Firestore query and document retrieval adapted from Firebase documentation
    // https://firebase.google.com/docs/firestore/query-data/get-data
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-string.html

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

                // RecyclerView adapter binding based on Android developer guide
                // https://developer.android.com/guide/topics/ui/layout/recyclerview
                recipeAdapter = RecipeCardsAdapter(recipeList) { recipe ->
                    navigateToRecipeDetailsFragment(recipe)
                }
                recipeRecyclerView.adapter = recipeAdapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun navigateToRecipeDetailsFragment(recipe: RecipeCardModel) {
        val recipeDetailsFragment = RecipeDetailsFragment()

        // Passing data between fragments using Bundle adapted from Android developer documentation
        // https://developer.android.com/guide/fragments/communicate
        // https://developer.android.com/reference/kotlin/android/os/Bundle
        val bundle = Bundle()
        bundle.putString("recipeUserId", recipe.recipeUserId)
        bundle.putString("recipeDescription", recipe.recipeDescription)
        bundle.putString("recipeTitle", recipe.recipeTitle)
        bundle.putString("avgRating", recipe.avgRating.toString())
        bundle.putString("difficultyLevel", recipe.difficultyLevel)
        bundle.putInt("cookingTime", recipe.cookingTime)
        bundle.putString("cuisineType", recipe.cuisineType)
        bundle.putString("recipeId", recipe.recipeId)

        recipeDetailsFragment.arguments = bundle

        // Fragment navigation and transactions adapted from Android developer documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        //https://rohitksingh.medium.com/what-is-addtobackstack-in-fragment-661ac01a6507
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}

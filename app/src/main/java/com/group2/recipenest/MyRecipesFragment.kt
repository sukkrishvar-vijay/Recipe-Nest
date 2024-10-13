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

class MyRecipesFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeCardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_recipes_collection, container, false)

        // Find the toolbar in the activity
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)

        // Set the toolbar title to the tile title passed from FavoritesFragment
        toolbar.title = "My Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.black, null))

        // Set up the back button (up button)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)  // Replace with your back icon
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()  // Navigate back when back button is clicked
        }

        // Sample data (this can be dynamic based on the collection)
        val recipeList = listOf(
            RecipeCardModel("Recipe 1", "Easy • 30mins • Thai, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 2", "Medium • 45mins • Italian, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 3", "Hard • 60mins • Mexican", R.drawable.placeholder_recipe_image), RecipeCardModel("Recipe 1", "Easy • 30mins • Thai, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 2", "Medium • 45mins • Italian, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 3", "Hard • 60mins • Mexican", R.drawable.placeholder_recipe_image), RecipeCardModel("Recipe 1", "Easy • 30mins • Thai, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 2", "Medium • 45mins • Italian, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 3", "Hard • 60mins • Mexican", R.drawable.placeholder_recipe_image), RecipeCardModel("Recipe 1", "Easy • 30mins • Thai, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 2", "Medium • 45mins • Italian, Vegetarian", R.drawable.placeholder_recipe_image),
            RecipeCardModel("Recipe 3", "Hard • 60mins • Mexican", R.drawable.placeholder_recipe_image)
        )

        // Initialize RecyclerView
        recipeRecyclerView = view.findViewById(R.id.my_recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recipeAdapter = RecipeCardsAdapter(recipeList)
        recipeRecyclerView.adapter = recipeAdapter

        return view
    }

//    companion object {
//        // Method to create a new instance of RecipeCardsFragment and pass the tile title
//        fun newInstance(tileTitle: String): RecipeCardsFragment {
//            val fragment = RecipeCardsFragment()
//            val args = Bundle()
//            args.putString("tileTitle", tileTitle)
//            fragment.arguments = args
//            return fragment
//        }
//    }
}

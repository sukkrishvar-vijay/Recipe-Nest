data class RecipeCardModel(
    val recipeTitle: String,
    val difficultyLevel: String,
    val cookingTime: Int,
    val cuisineType: String,
    val avgRating: Comparable<*>,
    val imageResId: Int
)

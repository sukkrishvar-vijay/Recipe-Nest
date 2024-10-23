data class RecipesCarouselModel(
    var recipeId: String,
    val recipeDescription: String,
    val recipeUserId: String,
    val recipeTitle: String,
    val difficultyLevel: String,
    val cookingTime: Int,
    val cuisineType: String,
    val avgRating: Comparable<*>,
    val imageResId: Int
)
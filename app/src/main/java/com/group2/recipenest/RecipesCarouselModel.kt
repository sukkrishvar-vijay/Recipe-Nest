import java.util.Date

data class RecipesCarouselModel(
    var recipeId: String,
    val recipeDescription: String,
    val recipeUserId: String,
    val recipeTitle: String,
    val difficultyLevel: String,
    val cookingTime: Int,
    val cuisineType: String,
    val avgRating: Double,
    val recipeImageUrl: String? = null,
    val dateRecipeAdded: Date
)
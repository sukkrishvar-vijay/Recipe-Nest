data class Reviews(
    val userName: String,
    val userId: String,
    val date: String,
    val reviewText: String,
    val rating: Float,
    val audioFilePath: String? = null
)
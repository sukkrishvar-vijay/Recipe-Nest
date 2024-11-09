import java.util.Date

data class ReviewModel(
    val fullName: String,
    val username: String,
    val comment: String,
    val dateCommented: Date,
    val rating: Int,
    val audioCommentUrl: String,
)

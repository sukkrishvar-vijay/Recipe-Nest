import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group2.recipenest.R

class RecipesCarouselAdapter(private val carouselItems: List<RecipesCarouselModel>) :
    RecyclerView.Adapter<RecipesCarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = carouselItems[position]
        holder.titleTextView.text = item.title
        holder.imageView.setImageResource(item.imageResId)
    }

    override fun getItemCount(): Int = carouselItems.size

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.recipeTitleTextView)
    }
}

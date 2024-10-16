import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.group2.recipenest.R

class AddRecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.add_recipe_fragment, container, false)

        // Set the toolbar title
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.title = "Add New Recipe"
        toolbar.setTitleTextColor(Color.BLACK)

        // Initialize UI elements
        val uploadImageButton: Button = rootView.findViewById(R.id.upload_image_button)
        val titleEditText: EditText = rootView.findViewById(R.id.recipe_title)
        val descriptionEditText: EditText = rootView.findViewById(R.id.recipe_description)
        val cuisineChinese: CheckBox = rootView.findViewById(R.id.cuisine_chinese)
        val difficultyToggleGroup: MaterialButtonToggleGroup = rootView.findViewById(R.id.difficulty_toggle_group)
        val easy_button: MaterialButton = rootView.findViewById(R.id.easy_button)
        val medium_button: MaterialButton = rootView.findViewById(R.id.medium_button)
        val hard_button: MaterialButton = rootView.findViewById(R.id.hard_button)

        // Handle image upload click (currently placeholder functionality)
        uploadImageButton.setOnClickListener {
            Toast.makeText(activity, "Upload image clicked", Toast.LENGTH_SHORT).show()
        }

        // Handle difficulty level toggle selection
        difficultyToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedButton: MaterialButton = rootView.findViewById(checkedId)
                setSelectedButtonState(selectedButton)
                if(selectedButton == easy_button){
                    resetButtonState(medium_button)
                    resetButtonState(hard_button)
                }else if (selectedButton == medium_button){
                    resetButtonState(easy_button)
                    resetButtonState(hard_button)
                }else{
                    resetButtonState(easy_button)
                    resetButtonState(medium_button)
                }
            }
        }

        return rootView
    }

    // Helper to set the selected button state
    private fun setSelectedButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#D1C300")) // Set selected background color
        button.setTextColor(Color.BLACK) // Change text color to white when selected
    }

    // Helper to reset button state to unselected
    private fun resetButtonState(button: MaterialButton) {
        button.setBackgroundColor(Color.parseColor("#FFFCD7")) // Set default unselected background color
        button.setTextColor(Color.BLACK)  // Set text color to black
    }
}

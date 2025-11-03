package com.example.android_tv_frontend.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.android_tv_frontend.R
import com.example.android_tv_frontend.data.model.Recipe
import com.example.android_tv_frontend.data.repository.MockRecipeRepository
import kotlinx.coroutines.launch

/**
 * Fragment displaying detailed recipe information.
 * Shows hero image, metadata, ingredients, and step-by-step instructions.
 */
class DetailFragment : Fragment() {
    
    private lateinit var viewModel: DetailViewModel
    private var recipeId: String? = null
    
    private lateinit var heroImage: ImageView
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var cuisineView: TextView
    private lateinit var prepTimeView: TextView
    private lateinit var cookTimeView: TextView
    private lateinit var totalTimeView: TextView
    private lateinit var difficultyView: TextView
    private lateinit var servingsView: TextView
    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var instructionsContainer: LinearLayout
    private lateinit var favoriteButton: Button
    private lateinit var backButton: Button
    private lateinit var loadingView: View
    private lateinit var contentView: View
    private lateinit var errorView: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        recipeId = arguments?.getString(ARG_RECIPE_ID)
        
        // Initialize ViewModel with mock repository
        val repository = MockRecipeRepository()
        viewModel = DetailViewModel(repository)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupButtons()
        observeViewModel()
        
        recipeId?.let { viewModel.loadRecipe(it) }
    }
    
    private fun setupViews(view: View) {
        heroImage = view.findViewById(R.id.hero_image)
        titleView = view.findViewById(R.id.recipe_title)
        descriptionView = view.findViewById(R.id.recipe_description)
        cuisineView = view.findViewById(R.id.cuisine_text)
        prepTimeView = view.findViewById(R.id.prep_time_text)
        cookTimeView = view.findViewById(R.id.cook_time_text)
        totalTimeView = view.findViewById(R.id.total_time_text)
        difficultyView = view.findViewById(R.id.difficulty_text)
        servingsView = view.findViewById(R.id.servings_text)
        ingredientsContainer = view.findViewById(R.id.ingredients_container)
        instructionsContainer = view.findViewById(R.id.instructions_container)
        favoriteButton = view.findViewById(R.id.favorite_button)
        backButton = view.findViewById(R.id.back_button)
        loadingView = view.findViewById(R.id.loading_view)
        contentView = view.findViewById(R.id.content_view)
        errorView = view.findViewById(R.id.error_view)
    }
    
    private fun setupButtons() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        favoriteButton.setOnClickListener {
            recipeId?.let { viewModel.toggleFavorite(it) }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DetailUiState.Loading -> showLoading()
                    is DetailUiState.Success -> showRecipe(state.recipe)
                    is DetailUiState.Error -> showError(state.message)
                }
            }
        }
    }
    
    private fun showLoading() {
        loadingView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        errorView.visibility = View.GONE
    }
    
    private fun showRecipe(recipe: Recipe) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        
        // Set placeholder image
        heroImage.setImageResource(R.drawable.ic_launcher)
        
        // TODO: Load image with Glide when image URLs are available
        // Glide.with(this)
        //     .load(recipe.imageUrl)
        //     .placeholder(R.drawable.ic_launcher)
        //     .into(heroImage)
        
        titleView.text = recipe.title
        descriptionView.text = recipe.description
        cuisineView.text = recipe.cuisine
        prepTimeView.text = "${recipe.prepTime} min"
        cookTimeView.text = "${recipe.cookTime} min"
        totalTimeView.text = "${recipe.totalTime} min"
        difficultyView.text = recipe.difficulty.toString()
        servingsView.text = "${recipe.servings} servings"
        
        // Update favorite button
        favoriteButton.text = if (recipe.isFavorite) "Remove from Favorites" else "Add to Favorites"
        
        // Populate ingredients
        ingredientsContainer.removeAllViews()
        recipe.ingredients.forEach { ingredient ->
            val textView = TextView(context).apply {
                text = "• ${ingredient.toDisplayString()}"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, R.color.tv_text_primary))
                setPadding(0, 8, 0, 8)
            }
            ingredientsContainer.addView(textView)
        }
        
        // Populate instructions
        instructionsContainer.removeAllViews()
        recipe.instructions.forEach { step ->
            val textView = TextView(context).apply {
                text = "${step.stepNumber}. ${step.instruction}"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, R.color.tv_text_primary))
                setPadding(0, 12, 0, 12)
            }
            instructionsContainer.addView(textView)
        }
    }
    
    private fun showError(message: String) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }
    
    companion object {
        private const val ARG_RECIPE_ID = "recipe_id"
        
        fun newInstance(recipeId: String): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECIPE_ID, recipeId)
                }
            }
        }
    }
}

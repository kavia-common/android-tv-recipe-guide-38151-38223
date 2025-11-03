package com.example.android_tv_frontend.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_tv_frontend.R
import com.example.android_tv_frontend.data.repository.MockRecipeRepository
import kotlinx.coroutines.launch

/**
 * Fragment displaying recipe grid with side navigation for filtering.
 * Uses TV-optimized layouts with D-pad navigation support.
 */
class BrowseFragment : Fragment() {
    
    private lateinit var viewModel: BrowseViewModel
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var filterAdapter: FilterAdapter
    
    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var loadingView: View
    private lateinit var emptyView: View
    private lateinit var errorView: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel with mock repository
        val repository = MockRecipeRepository()
        viewModel = BrowseViewModel(repository)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecipeGrid()
        setupFilters()
        observeViewModel()
    }
    
    private fun setupViews(view: View) {
        filterRecyclerView = view.findViewById(R.id.filter_recycler_view)
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        loadingView = view.findViewById(R.id.loading_view)
        emptyView = view.findViewById(R.id.empty_view)
        errorView = view.findViewById(R.id.error_view)
    }
    
    private fun setupRecipeGrid() {
        recipeAdapter = RecipeAdapter { recipe ->
            navigateToDetail(recipe.id)
        }
        
        recipeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3) // 3 columns for TV
            adapter = recipeAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupFilters() {
        filterAdapter = FilterAdapter { filter ->
            viewModel.applyFilter(filter)
        }
        
        filterRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = filterAdapter
        }
        
        // Observe available cuisines
        lifecycleScope.launch {
            viewModel.availableCuisines.collect { cuisines ->
                filterAdapter.updateCuisines(cuisines)
            }
        }
        
        // Observe selected filter
        lifecycleScope.launch {
            viewModel.selectedFilter.collect { filter ->
                filterAdapter.updateSelectedFilter(filter)
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is BrowseUiState.Loading -> showLoading()
                    is BrowseUiState.Success -> showRecipes(state.recipes)
                    is BrowseUiState.Empty -> showEmpty()
                    is BrowseUiState.Error -> showError(state.message)
                }
            }
        }
    }
    
    private fun showLoading() {
        loadingView.visibility = View.VISIBLE
        recipeRecyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
    }
    
    private fun showRecipes(recipes: List<com.example.android_tv_frontend.data.model.Recipe>) {
        loadingView.visibility = View.GONE
        recipeRecyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
        
        recipeAdapter.submitList(recipes)
    }
    
    private fun showEmpty() {
        loadingView.visibility = View.GONE
        recipeRecyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        loadingView.visibility = View.GONE
        recipeRecyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }
    
    private fun navigateToDetail(recipeId: String) {
        val fragment = com.example.android_tv_frontend.ui.detail.DetailFragment.newInstance(recipeId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    companion object {
        fun newInstance() = BrowseFragment()
    }
}

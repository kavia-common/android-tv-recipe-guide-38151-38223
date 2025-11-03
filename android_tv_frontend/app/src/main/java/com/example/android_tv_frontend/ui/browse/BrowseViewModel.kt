package com.example.android_tv_frontend.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_tv_frontend.data.model.Recipe
import com.example.android_tv_frontend.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Browse screen.
 * Manages recipe data loading, filtering, and UI state.
 */
class BrowseViewModel(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState
    
    private val _selectedFilter = MutableStateFlow<FilterType>(FilterType.All)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter
    
    private val _availableCuisines = MutableStateFlow<List<String>>(emptyList())
    val availableCuisines: StateFlow<List<String>> = _availableCuisines
    
    init {
        loadRecipes()
        loadCuisines()
    }
    
    /**
     * Load recipes based on current filter
     */
    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = BrowseUiState.Loading
            
            try {
                val flow = when (val filter = _selectedFilter.value) {
                    is FilterType.All -> repository.getAllRecipes()
                    is FilterType.Cuisine -> repository.getRecipesByCuisine(filter.cuisine)
                    is FilterType.Favorites -> repository.getFavoriteRecipes()
                }
                
                flow.catch { error ->
                    _uiState.value = BrowseUiState.Error(error.message ?: "Unknown error")
                }.collect { recipes ->
                    if (recipes.isEmpty()) {
                        _uiState.value = BrowseUiState.Empty
                    } else {
                        _uiState.value = BrowseUiState.Success(recipes)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BrowseUiState.Error(e.message ?: "Failed to load recipes")
            }
        }
    }
    
    /**
     * Load available cuisine types
     */
    private fun loadCuisines() {
        _availableCuisines.value = repository.getAvailableCuisines()
    }
    
    /**
     * Apply filter to recipe list
     */
    fun applyFilter(filter: FilterType) {
        _selectedFilter.value = filter
        loadRecipes()
    }
    
    /**
     * Toggle favorite status of a recipe
     */
    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId)
        }
    }
}

/**
 * UI state for Browse screen
 */
sealed class BrowseUiState {
    object Loading : BrowseUiState()
    object Empty : BrowseUiState()
    data class Success(val recipes: List<Recipe>) : BrowseUiState()
    data class Error(val message: String) : BrowseUiState()
}

/**
 * Filter types for recipe browsing
 */
sealed class FilterType {
    object All : FilterType()
    object Favorites : FilterType()
    data class Cuisine(val cuisine: String) : FilterType()
}

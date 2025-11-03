package com.example.android_tv_frontend.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_tv_frontend.data.model.Recipe
import com.example.android_tv_frontend.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Detail screen.
 * Manages single recipe data and favorite toggling.
 */
class DetailViewModel(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState
    
    /**
     * Load recipe details by ID
     */
    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            
            try {
                val recipe = repository.getRecipeById(recipeId)
                if (recipe != null) {
                    _uiState.value = DetailUiState.Success(recipe)
                } else {
                    _uiState.value = DetailUiState.Error("Recipe not found")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Failed to load recipe")
            }
        }
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId)
            // Reload to get updated favorite status
            loadRecipe(recipeId)
        }
    }
}

/**
 * UI state for Detail screen
 */
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val recipe: Recipe) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

package com.example.android_tv_frontend.data.repository

import com.example.android_tv_frontend.data.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * PUBLIC_INTERFACE
 * Repository interface for recipe data access.
 * This abstraction allows swapping between mock data, local database, or remote API.
 */
interface RecipeRepository {
    
    /**
     * Get all recipes as a Flow for reactive updates
     */
    fun getAllRecipes(): Flow<List<Recipe>>
    
    /**
     * Get recipes filtered by cuisine
     * @param cuisine The cuisine type to filter by
     */
    fun getRecipesByCuisine(cuisine: String): Flow<List<Recipe>>
    
    /**
     * Get favorite recipes
     */
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    
    /**
     * Get a single recipe by ID
     * @param recipeId The unique identifier of the recipe
     */
    suspend fun getRecipeById(recipeId: String): Recipe?
    
    /**
     * Toggle favorite status of a recipe
     * @param recipeId The unique identifier of the recipe
     */
    suspend fun toggleFavorite(recipeId: String)
    
    /**
     * Get available cuisine types
     */
    fun getAvailableCuisines(): List<String>
}

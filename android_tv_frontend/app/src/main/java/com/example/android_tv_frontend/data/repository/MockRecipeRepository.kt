package com.example.android_tv_frontend.data.repository

import com.example.android_tv_frontend.data.model.Difficulty
import com.example.android_tv_frontend.data.model.Ingredient
import com.example.android_tv_frontend.data.model.InstructionStep
import com.example.android_tv_frontend.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Mock implementation of RecipeRepository using in-memory data.
 * This can be replaced with a real implementation connecting to a backend API or database.
 */
class MockRecipeRepository : RecipeRepository {
    
    // Mutable state to allow toggling favorites
    private val recipesState = MutableStateFlow(createMockRecipes())
    
    override fun getAllRecipes(): Flow<List<Recipe>> = recipesState
    
    override fun getRecipesByCuisine(cuisine: String): Flow<List<Recipe>> {
        return recipesState.map { recipes ->
            recipes.filter { it.cuisine.equals(cuisine, ignoreCase = true) }
        }
    }
    
    override fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipesState.map { recipes ->
            recipes.filter { it.isFavorite }
        }
    }
    
    override suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipesState.value.find { it.id == recipeId }
    }
    
    override suspend fun toggleFavorite(recipeId: String) {
        val currentRecipes = recipesState.value.toMutableList()
        val index = currentRecipes.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            currentRecipes[index] = currentRecipes[index].copy(
                isFavorite = !currentRecipes[index].isFavorite
            )
            recipesState.value = currentRecipes
        }
    }
    
    override fun getAvailableCuisines(): List<String> {
        return listOf("Italian", "Mexican", "Asian", "American", "Mediterranean", "Indian")
    }
    
    /**
     * Creates a list of mock recipes for demonstration
     */
    private fun createMockRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "1",
                title = "Classic Margherita Pizza",
                description = "Traditional Italian pizza with fresh mozzarella, tomatoes, and basil",
                imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=800",
                cuisine = "Italian",
                prepTime = 20,
                cookTime = 15,
                difficulty = Difficulty.MEDIUM,
                servings = 4,
                ingredients = listOf(
                    Ingredient("Pizza dough", "1", "ball"),
                    Ingredient("Tomato sauce", "1/2", "cup"),
                    Ingredient("Fresh mozzarella", "8", "oz"),
                    Ingredient("Fresh basil", "10", "leaves"),
                    Ingredient("Olive oil", "2", "tbsp")
                ),
                instructions = listOf(
                    InstructionStep(1, "Preheat oven to 475°F (245°C)"),
                    InstructionStep(2, "Roll out pizza dough on a floured surface"),
                    InstructionStep(3, "Spread tomato sauce evenly over the dough"),
                    InstructionStep(4, "Add sliced mozzarella and drizzle with olive oil"),
                    InstructionStep(5, "Bake for 12-15 minutes until crust is golden"),
                    InstructionStep(6, "Top with fresh basil leaves before serving")
                )
            ),
            Recipe(
                id = "2",
                title = "Chicken Tacos",
                description = "Flavorful Mexican tacos with seasoned chicken and fresh toppings",
                imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=800",
                cuisine = "Mexican",
                prepTime = 15,
                cookTime = 20,
                difficulty = Difficulty.EASY,
                servings = 6,
                ingredients = listOf(
                    Ingredient("Chicken breast", "1", "lb"),
                    Ingredient("Taco seasoning", "2", "tbsp"),
                    Ingredient("Tortillas", "12", "pieces"),
                    Ingredient("Lettuce", "2", "cups"),
                    Ingredient("Tomatoes", "2", "medium"),
                    Ingredient("Cheese", "1", "cup"),
                    Ingredient("Sour cream", "1/2", "cup")
                ),
                instructions = listOf(
                    InstructionStep(1, "Cut chicken into bite-sized pieces"),
                    InstructionStep(2, "Season chicken with taco seasoning"),
                    InstructionStep(3, "Cook chicken in a skillet over medium heat for 8-10 minutes"),
                    InstructionStep(4, "Warm tortillas in another pan"),
                    InstructionStep(5, "Assemble tacos with chicken, lettuce, tomatoes, and cheese"),
                    InstructionStep(6, "Top with sour cream and serve")
                )
            ),
            Recipe(
                id = "3",
                title = "Pad Thai",
                description = "Classic Thai stir-fried noodles with shrimp and vegetables",
                imageUrl = "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=800",
                cuisine = "Asian",
                prepTime = 25,
                cookTime = 15,
                difficulty = Difficulty.MEDIUM,
                servings = 4,
                ingredients = listOf(
                    Ingredient("Rice noodles", "8", "oz"),
                    Ingredient("Shrimp", "1", "lb"),
                    Ingredient("Eggs", "2", "large"),
                    Ingredient("Bean sprouts", "2", "cups"),
                    Ingredient("Green onions", "4", "stalks"),
                    Ingredient("Peanuts", "1/4", "cup"),
                    Ingredient("Tamarind paste", "3", "tbsp"),
                    Ingredient("Fish sauce", "2", "tbsp")
                ),
                instructions = listOf(
                    InstructionStep(1, "Soak rice noodles in warm water for 30 minutes"),
                    InstructionStep(2, "Prepare sauce by mixing tamarind paste and fish sauce"),
                    InstructionStep(3, "Heat oil in a wok and scramble eggs"),
                    InstructionStep(4, "Add shrimp and cook until pink"),
                    InstructionStep(5, "Add drained noodles and sauce, stir-fry for 3-4 minutes"),
                    InstructionStep(6, "Add bean sprouts and green onions, toss well"),
                    InstructionStep(7, "Serve topped with crushed peanuts")
                )
            ),
            Recipe(
                id = "4",
                title = "Classic Burger",
                description = "Juicy American-style burger with all the fixings",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800",
                cuisine = "American",
                prepTime = 10,
                cookTime = 15,
                difficulty = Difficulty.EASY,
                servings = 4,
                ingredients = listOf(
                    Ingredient("Ground beef", "1.5", "lb"),
                    Ingredient("Burger buns", "4", "pieces"),
                    Ingredient("Lettuce", "4", "leaves"),
                    Ingredient("Tomato", "1", "large"),
                    Ingredient("Onion", "1", "medium"),
                    Ingredient("Cheese slices", "4", "pieces"),
                    Ingredient("Pickles", "8", "slices")
                ),
                instructions = listOf(
                    InstructionStep(1, "Form ground beef into 4 equal patties"),
                    InstructionStep(2, "Season patties with salt and pepper"),
                    InstructionStep(3, "Grill or pan-fry patties for 4-5 minutes per side"),
                    InstructionStep(4, "Add cheese slices during last minute of cooking"),
                    InstructionStep(5, "Toast burger buns lightly"),
                    InstructionStep(6, "Assemble burgers with lettuce, tomato, onion, and pickles")
                )
            ),
            Recipe(
                id = "5",
                title = "Greek Salad",
                description = "Fresh Mediterranean salad with feta cheese and olives",
                imageUrl = "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=800",
                cuisine = "Mediterranean",
                prepTime = 15,
                cookTime = 0,
                difficulty = Difficulty.EASY,
                servings = 4,
                ingredients = listOf(
                    Ingredient("Cucumber", "2", "large"),
                    Ingredient("Tomatoes", "4", "medium"),
                    Ingredient("Red onion", "1", "medium"),
                    Ingredient("Feta cheese", "8", "oz"),
                    Ingredient("Kalamata olives", "1", "cup"),
                    Ingredient("Olive oil", "1/4", "cup"),
                    Ingredient("Lemon juice", "2", "tbsp")
                ),
                instructions = listOf(
                    InstructionStep(1, "Chop cucumbers, tomatoes, and onion into bite-sized pieces"),
                    InstructionStep(2, "Place vegetables in a large bowl"),
                    InstructionStep(3, "Add olives and crumbled feta cheese"),
                    InstructionStep(4, "Drizzle with olive oil and lemon juice"),
                    InstructionStep(5, "Toss gently to combine"),
                    InstructionStep(6, "Serve immediately or chill for 30 minutes")
                )
            ),
            Recipe(
                id = "6",
                title = "Butter Chicken",
                description = "Creamy Indian curry with tender chicken in tomato sauce",
                imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800",
                cuisine = "Indian",
                prepTime = 30,
                cookTime = 40,
                difficulty = Difficulty.HARD,
                servings = 6,
                ingredients = listOf(
                    Ingredient("Chicken thighs", "2", "lb"),
                    Ingredient("Yogurt", "1", "cup"),
                    Ingredient("Tomato sauce", "2", "cups"),
                    Ingredient("Heavy cream", "1", "cup"),
                    Ingredient("Butter", "4", "tbsp"),
                    Ingredient("Garam masala", "2", "tbsp"),
                    Ingredient("Ginger-garlic paste", "2", "tbsp")
                ),
                instructions = listOf(
                    InstructionStep(1, "Marinate chicken in yogurt and spices for 30 minutes"),
                    InstructionStep(2, "Heat butter in a large pan over medium heat"),
                    InstructionStep(3, "Add ginger-garlic paste and cook until fragrant"),
                    InstructionStep(4, "Add marinated chicken and cook until browned"),
                    InstructionStep(5, "Add tomato sauce and simmer for 20 minutes"),
                    InstructionStep(6, "Stir in cream and garam masala"),
                    InstructionStep(7, "Simmer for 10 more minutes and serve with rice")
                )
            )
        )
    }
}

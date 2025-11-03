package com.example.android_tv_frontend.data.model

/**
 * Recipe data model representing a recipe with its details, ingredients, and instructions.
 * 
 * @property id Unique identifier for the recipe
 * @property title Name of the recipe
 * @property description Short description of the recipe
 * @property imageUrl URL to the recipe hero image
 * @property cuisine Type of cuisine (e.g., Italian, Mexican, Asian)
 * @property prepTime Preparation time in minutes
 * @property cookTime Cooking time in minutes
 * @property difficulty Difficulty level (Easy, Medium, Hard)
 * @property servings Number of servings
 * @property ingredients List of ingredients with amounts
 * @property instructions Step-by-step cooking instructions
 * @property isFavorite Whether the recipe is marked as favorite
 */
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val cuisine: String,
    val prepTime: Int, // in minutes
    val cookTime: Int, // in minutes
    val difficulty: Difficulty,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<InstructionStep>,
    val isFavorite: Boolean = false
) {
    /**
     * Total time required for the recipe (prep + cook time)
     */
    val totalTime: Int
        get() = prepTime + cookTime
}

/**
 * Represents a single ingredient with its amount and unit
 */
data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String
) {
    /**
     * Formatted ingredient string for display
     */
    fun toDisplayString(): String = "$amount $unit $name"
}

/**
 * Represents a single instruction step
 */
data class InstructionStep(
    val stepNumber: Int,
    val instruction: String
)

/**
 * Recipe difficulty levels
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;
    
    override fun toString(): String {
        return when (this) {
            EASY -> "Easy"
            MEDIUM -> "Medium"
            HARD -> "Hard"
        }
    }
}

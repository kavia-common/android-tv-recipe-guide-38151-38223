package com.example.android_tv_frontend.ui.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android_tv_frontend.R
import com.example.android_tv_frontend.data.model.Recipe

/**
 * Adapter for displaying recipes in a grid layout.
 * Handles D-pad navigation and focus states for TV.
 */
class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view, onRecipeClick)
    }
    
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class RecipeViewHolder(
        itemView: View,
        private val onRecipeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val imageView: ImageView = itemView.findViewById(R.id.recipe_image)
        private val titleView: TextView = itemView.findViewById(R.id.recipe_title)
        private val cuisineView: TextView = itemView.findViewById(R.id.recipe_cuisine)
        private val timeView: TextView = itemView.findViewById(R.id.recipe_time)
        private val difficultyView: TextView = itemView.findViewById(R.id.recipe_difficulty)
        
        fun bind(recipe: Recipe) {
            titleView.text = recipe.title
            cuisineView.text = recipe.cuisine
            timeView.text = "${recipe.totalTime} min"
            difficultyView.text = recipe.difficulty.toString()
            
            // Set placeholder image (in production, use Glide to load from URL)
            imageView.setImageResource(R.drawable.ic_launcher)
            
            // TODO: Load image with Glide when image URLs are available
            // Glide.with(itemView.context)
            //     .load(recipe.imageUrl)
            //     .placeholder(R.drawable.ic_launcher)
            //     .into(imageView)
            
            itemView.setOnClickListener {
                onRecipeClick(recipe)
            }
            
            // Enable focus for TV navigation
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
    
    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}

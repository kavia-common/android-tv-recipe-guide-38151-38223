package com.example.android_tv_frontend.ui.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_tv_frontend.R

/**
 * Adapter for displaying filter options in side navigation.
 */
class FilterAdapter(
    private val onFilterClick: (FilterType) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
    
    private val filters = mutableListOf<FilterItem>()
    private var selectedFilterType: FilterType = FilterType.All
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return FilterViewHolder(view, onFilterClick)
    }
    
    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val item = filters[position]
        val isSelected = when {
            item.filterType is FilterType.All && selectedFilterType is FilterType.All -> true
            item.filterType is FilterType.Favorites && selectedFilterType is FilterType.Favorites -> true
            item.filterType is FilterType.Cuisine && selectedFilterType is FilterType.Cuisine -> {
                item.filterType.cuisine == (selectedFilterType as FilterType.Cuisine).cuisine
            }
            else -> false
        }
        holder.bind(item, isSelected)
    }
    
    override fun getItemCount(): Int = filters.size
    
    fun updateCuisines(cuisines: List<String>) {
        filters.clear()
        filters.add(FilterItem("All Recipes", FilterType.All))
        filters.add(FilterItem("Favorites", FilterType.Favorites))
        
        cuisines.forEach { cuisine ->
            filters.add(FilterItem(cuisine, FilterType.Cuisine(cuisine)))
        }
        
        notifyDataSetChanged()
    }
    
    fun updateSelectedFilter(filter: FilterType) {
        selectedFilterType = filter
        notifyDataSetChanged()
    }
    
    class FilterViewHolder(
        itemView: View,
        private val onFilterClick: (FilterType) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val textView: TextView = itemView.findViewById(R.id.filter_text)
        
        fun bind(item: FilterItem, isSelected: Boolean) {
            textView.text = item.label
            
            // Highlight selected filter
            itemView.isActivated = isSelected
            
            itemView.setOnClickListener {
                onFilterClick(item.filterType)
            }
            
            // Enable focus for TV navigation
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
    
    data class FilterItem(
        val label: String,
        val filterType: FilterType
    )
}

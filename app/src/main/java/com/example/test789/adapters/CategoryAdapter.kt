package com.example.test789.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.test789.R
import com.example.test789.models.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: OnCategoryClick
): RecyclerView.Adapter<CategoryAdapter.CategoryHolder>(){

    interface OnCategoryClick {
        fun onCategoryClicked(position: Int)
    }
    inner class CategoryHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val category : TextView = itemView.findViewById(R.id.categoryName)
        val item : CardView = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent , false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val currentCategory = categories[position]
        holder.category.text = currentCategory.category
        holder.item.setOnClickListener { onCategoryClick.onCategoryClicked(position) }
    }
}
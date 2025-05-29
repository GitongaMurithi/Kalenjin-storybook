package com.example.test789.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test789.R
import com.example.test789.models.AttemptedBooks
import javax.inject.Inject

class AttemptedAdapter @Inject constructor(
    private val stories: List<AttemptedBooks>
): RecyclerView.Adapter<AttemptedAdapter.AttemptedViewHolder>() {

    inner class AttemptedViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle : TextView = itemView.findViewById(R.id.edit_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttemptedViewHolder {
        return AttemptedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.attempted_item, parent ,false))
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    override fun onBindViewHolder(holder: AttemptedViewHolder, position: Int) {
        val story = stories[position]
        holder.bookTitle.text = story.title
    }
}
package com.example.test789.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test789.R
import com.example.test789.models.CompletedBooks
import javax.inject.Inject

class CompletedBooksAdapter @Inject constructor(
    private val stories: List<CompletedBooks>
): RecyclerView.Adapter<CompletedBooksAdapter.CompletedBooksViewHolder>() {

    inner class CompletedBooksViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle : TextView = itemView.findViewById(R.id.edit_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedBooksViewHolder {
        return CompletedBooksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.read_item, parent ,false))
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    override fun onBindViewHolder(holder: CompletedBooksViewHolder, position: Int) {
        val story = stories[position]
        holder.bookTitle.text = story.title
    }
}
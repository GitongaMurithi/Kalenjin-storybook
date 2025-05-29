package com.example.test789.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test789.R
import com.example.test789.models.Story
import javax.inject.Inject

class AuthorStoriesAdapter @Inject constructor(
    private val stories : ArrayList<Story>,
    private val onAction: OnAction,
) : RecyclerView.Adapter<AuthorStoriesAdapter.AuthorStoriesViewHolder>(){

    interface OnAction {
        fun onEditClick (position: Int)
        fun onStoryClicked (position: Int)
        fun onDeleteClicked (position: Int)
    }
    inner class AuthorStoriesViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val edit : ImageView = itemView.findViewById(R.id.edit)
        val delete : ImageView = itemView.findViewById(R.id.delete)
        val title : TextView = itemView.findViewById(R.id.edit_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorStoriesViewHolder {
        return AuthorStoriesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_story_item , parent , false))
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    override fun onBindViewHolder(holder: AuthorStoriesViewHolder, position: Int) {
        val story = stories[position]

        holder.title.text = story.title
        holder.edit.setOnClickListener { onAction.onEditClick(position) }
        holder.delete.setOnClickListener {
            onAction.onDeleteClicked(position)
        }
        holder.itemView.setOnClickListener { onAction.onStoryClicked(position) }
    }
}
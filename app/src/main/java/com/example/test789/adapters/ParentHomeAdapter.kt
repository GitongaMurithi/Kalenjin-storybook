package com.example.test789.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test789.R
import com.example.test789.models.Story
import javax.inject.Inject

class ParentHomeAdapter @Inject constructor(
    private val context: Context,
    private var stories : ArrayList<Story>,
    private val onStoryClicked: OnStoryClicked
): RecyclerView.Adapter<ParentHomeAdapter.ParentViewHolder>() {

    interface OnStoryClicked {
        fun onStoryClicked (position: Int)
    }

    inner class ParentViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val author : TextView = itemView.findViewById(R.id.author)
        private val title : TextView = itemView.findViewById(R.id.title)
        val image : ImageView = itemView.findViewById(R.id.storyImage)
        private val card : ConstraintLayout = itemView.findViewById(R.id.story_holder)

        fun bind (story: Story, position: Int) {
            author.text = story.author
            title.text = story.title
            card.setOnClickListener { onStoryClicked.onStoryClicked(position) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        return ParentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.story_item, parent , false))
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val currentStory = stories[position]
        holder.bind(currentStory , position)

        Glide.with(context).load(currentStory.image).placeholder(R.drawable.baseline_menu_book_24).error(
            R.drawable.baseline_menu_book_24
        ).into(holder.image)
    }
}
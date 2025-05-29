package com.example.test789.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test789.R
import com.example.test789.models.Review
import javax.inject.Inject

class ReviewsAdapter @Inject constructor(
    private val reviews : ArrayList<Review>
): RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val value : TextView = itemView.findViewById(R.id.feedback)

        fun bind(review: Review) {
            value.text = review.review
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        return ReviewsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent , false))
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val currentReview = reviews[position]
        holder.bind(currentReview)
    }
}
package com.example.test789.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test789.R
import com.example.test789.models.Child
import javax.inject.Inject

class ChildProfilesAdapter @Inject constructor(
    private val context: Context,
    private val children : ArrayList<Child>,
    private val onChildClick: OnChildClick
) : RecyclerView.Adapter<ChildProfilesAdapter.ChildProfilesViewHolder>(){

    interface OnChildClick {
        fun onChildClicked (position: Int)
    }

    inner class ChildProfilesViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.child_name)
        val dp : ImageView = itemView.findViewById(R.id.child_dp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildProfilesViewHolder {
        return ChildProfilesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profiles_item, parent , false))
    }

    override fun getItemCount(): Int {
        return children.size
    }

    override fun onBindViewHolder(holder: ChildProfilesViewHolder, position: Int) {
        val currentChild = children[position]

        holder.name.text = currentChild.name.split(" ")[0]
        holder.itemView.setOnClickListener {
            onChildClick.onChildClicked(position)
        }

        Glide.with(context).load(currentChild.image).placeholder(R.drawable.baseline_account_circle_24).error(
            R.drawable.baseline_account_circle_24
        ).into(holder.dp)

    }
}
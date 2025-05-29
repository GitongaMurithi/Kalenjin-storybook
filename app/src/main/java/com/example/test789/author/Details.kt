package com.example.test789.author

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.adapters.ReviewsAdapter
import com.example.test789.databinding.ActivityDetailsBinding
import com.example.test789.models.Review
import com.example.test789.models.Story
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Details : AppCompatActivity() {

    private lateinit var binding : ActivityDetailsBinding
    private lateinit var reviews: ArrayList<Review>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id").toString()
        reviews = ArrayList()

        FirebaseDatabase.getInstance().getReference("Stories")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { story ->
                        if (story.getValue(Story::class.java)!!.id == id) {
                            binding.author.text = "By ${story.getValue(Story::class.java)!!.author}"
                            binding.title.text = story.getValue(Story::class.java)!!.title
                            binding.pages.text =
                                "${story.getValue(Story::class.java)!!.pages} pages"
                            binding.read.text =
                                "Read by ${story.getValue(Story::class.java)!!.read}"
                            binding.likes.text =
                                "Liked by ${story.getValue(Story::class.java)!!.liked}"
                            binding.dislikes.text =
                                "Disliked by ${story.getValue(Story::class.java)!!.disliked}"
                            binding.descriptionV.text =
                                story.getValue(Story::class.java)!!.description
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        FirebaseDatabase.getInstance().getReference("Reviews/$id")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviews.clear()
                    if (snapshot.exists()) {
                        snapshot.children.forEach { dataSnapshot ->
                            val bookId = dataSnapshot.getValue(Review::class.java)!!.bookId
                            if (id == bookId) {
                                reviews.add(dataSnapshot.getValue(Review::class.java)!!)
                                binding.reviewsV.adapter = ReviewsAdapter(reviews = reviews)
                                binding.reviewsV.visibility = View.VISIBLE
                                binding.reviewsLoading.visibility = View.GONE
                            } else {
                                binding.reviewsLoading.visibility = View.GONE
                                binding.reviewsV.visibility = View.GONE
                                binding.empty.visibility = View.VISIBLE
                            }
                        }

                    } else {
                        binding.reviewsLoading.visibility = View.GONE
                        binding.reviewsV.visibility = View.GONE
                        binding.empty.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
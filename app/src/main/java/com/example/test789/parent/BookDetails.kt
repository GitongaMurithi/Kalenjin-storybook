package com.example.test789.parent

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.ReviewsAdapter
import com.example.test789.databinding.ActivityBookDetailsBinding
import com.example.test789.models.AttemptedBooks
import com.example.test789.models.Review
import com.example.test789.models.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class BookDetails : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var reviews: ArrayList<Review>
    private lateinit var childId: String
    private lateinit var title: String
    private lateinit var pushId: String
    private lateinit var userId: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        reviews = ArrayList()

        binding.reviewsV.setHasFixedSize(true)
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.reviewsV.layoutManager = manager


        val id = intent.getStringExtra("id").toString()
        childId = intent.getStringExtra("childId").toString()
        title = intent.getStringExtra("title").toString()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        pushId = FirebaseDatabase.getInstance().getReference("Children/$userId")
            .child(childId).child("attemptedBooks").push().key!!


        binding.back.setOnClickListener { finish() }

//        val sharedPreferences = getSharedPreferences ("MyPrefs" , MODE_PRIVATE)
//        val edit = sharedPreferences.edit()
//        edit.putString("id" , id)
//        edit.apply()

        FirebaseDatabase.getInstance().getReference("Stories")
            .addValueEventListener(object : ValueEventListener {
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


        binding.button.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Stories/$id").child("readBy")
                .child(childId)
                .get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        FirebaseDatabase.getInstance().getReference("Stories/$id")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val doc = currentData.getValue(Story::class.java)!!
                                    val currentRead = doc.read
                                    doc.read = currentRead + 1
                                    doc.readBy?.put(childId, true) ?: run {
                                        doc.readBy = mutableMapOf(childId to true)
                                    }

                                    currentData.value = doc
                                    return Transaction.success(currentData)
                                }

                                override fun onComplete(
                                    error: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    if (committed) {
                                        Log.d("ReadCount", "Read count updated successfully")
                                    } else {
                                        Toast.makeText(
                                            this@BookDetails,
                                            "Transaction failed: ${error?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            }
                            )
                    } else {
                        Toast.makeText(
                            this@BookDetails,
                            "You have already read the story",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val attemptedBook = AttemptedBooks(
                        id = id,
                        title = title
                    )

                    if (attemptedBook.id.isNotBlank() && attemptedBook.title.isNotBlank()) {
                        FirebaseDatabase.getInstance().getReference("Children/$userId")
                            .child(childId).child("attemptedBooks/$id")
                            .setValue(
                                attemptedBook
                            )
//                            .addOnSuccessListener {
//                                FirebaseDatabase.getInstance().getReference("Children/$userId")
//                                    .child(childId).child("attemptedBooks").child("id").removeValue()
//                                FirebaseDatabase.getInstance().getReference("Children/$userId")
//                                    .child(childId).child("attemptedBooks").child("title").removeValue()
//                            }
                    }

                    launchReading(id = id)
                }

        }

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

    private fun launchReading(id: String) {
        FirebaseDatabase.getInstance().getReference("Stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { story ->
                        if (story.getValue(Story::class.java)!!.id == id) {
                            startActivity(
                                Intent(this@BookDetails, Read::class.java)
                                    .putExtra(
                                        "content",
                                        story.getValue(Story::class.java)!!.content.url
                                    )
                                    .putExtra("id", id)
                                    .putExtra("childId", childId)
                                    .putExtra("title", title)
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
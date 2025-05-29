package com.example.test789.parent

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityReadBinding
import com.example.test789.models.CompletedBooks
import com.example.test789.models.Review
import com.example.test789.models.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class Read : AppCompatActivity() {

    private lateinit var binding: ActivityReadBinding
    private lateinit var userId: String
    private var isBookCompleted: Boolean = false
    private lateinit var bookId: String
    private lateinit var childId: String
    private lateinit var reviewId: String
    private lateinit var contentUrl: String
    private lateinit var title: String
    private var hasDisplayedPDF = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userId = FirebaseAuth.getInstance().currentUser!!.uid
        bookId = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()
        childId = intent.getStringExtra("childId").toString()
        reviewId = FirebaseDatabase.getInstance().getReference("Reviews").push().key!!
        contentUrl = intent.getStringExtra("content").toString()


        if (!hasDisplayedPDF) {
            downloadAndDisplayPDF(contentUrl)
            hasDisplayedPDF = true
        }

        binding.like.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Stories/$bookId").child("likedBy")
                .child(childId)
                .get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        FirebaseDatabase.getInstance().getReference("Stories/$bookId")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val doc = currentData.getValue(Story::class.java)!!
                                    val currentLikes = doc.liked
                                    doc.liked = currentLikes + 1
                                    doc.likedBy?.put(childId, true) ?: run {
                                        doc.likedBy = mutableMapOf(childId to true)
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
                                        Toast.makeText(this@Read, "Liked", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            this@Read,
                                            "Transaction failed: ${error?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            })
                    } else {
                        Toast.makeText(this@Read, "You have already liked the story", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.dislike.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("Stories/$bookId").child("dislikedBy")
                .child(childId)
                .get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        FirebaseDatabase.getInstance().getReference("Stories/$bookId")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val doc = currentData.getValue(Story::class.java)!!
                                    val currentDislikes = doc.disliked
                                    doc.disliked = currentDislikes + 1
                                    doc.dislikedBy?.put(childId, true) ?: run {
                                        doc.dislikedBy = mutableMapOf(childId to true)
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
                                        startActivity(
                                            Intent(
                                                this@Read, Dislike::class.java
                                            )
                                                .putExtra("childId", childId)
                                        )
                                        finish()
                                        overridePendingTransition(0,0)
                                        Toast.makeText(
                                            this@Read,
                                            "Disliked",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@Read,
                                            "Transaction failed: ${error?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            })
                    } else {
                        Toast.makeText(
                            this@Read,
                            "You have already disliked the story",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        }

        binding.send.setOnClickListener {
            if (binding.myReview.text.isEmpty()) {
                binding.myReview.error = "This is a required field."
                binding.myReview.requestFocus()
                return@setOnClickListener
            } else {
                binding.sending.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().getReference("Reviews/$bookId/$reviewId")
                    .setValue(
                        Review(
                            bookId = bookId,
                            review = binding.myReview.text.toString()
                        )
                    )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.sending.visibility = View.GONE
                            binding.myReview.text.clear()
                            Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ParentDashboard::class.java))
                            finish()
                        } else {
                            binding.sending.visibility = View.GONE
                            Toast.makeText(
                                this,
                                task.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }


    }

    private fun downloadAndDisplayPDF(contentUrl: String) {
        val request = Request.Builder().url(contentUrl).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Read, "Download failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body?.byteStream()
                runOnUiThread {
                    inputStream?.let {
                        binding.pdfview.fromStream(it)
                            .defaultPage(0)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .onPageChange { page, pageCount ->
                                val progress = ((page + 1).toFloat() / pageCount) * 100
                                if (!isBookCompleted && progress >= 100) {
                                    isBookCompleted = true
                                    markBookAsCompleted(bookId)
                                }
                                updateReadingProgress(progress)
                            }
                            .load()

                        binding.loadingContent.visibility = View.GONE
                    }
                }
            }
        }
        )
    }

    private fun markBookAsCompleted(bookId: String) {
        val completedBook = CompletedBooks(
            id = bookId,
            title = title
        )
        if (completedBook.id.isNotBlank() && completedBook.title.isNotBlank()) {
            FirebaseDatabase.getInstance().getReference("Children/$userId")
                .child(childId).child("completedBooks/$bookId")
                .setValue(
                    completedBook
                )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateReadingProgress(progress: Float) {
        binding.textView2.text = "Read ${progress.toInt()} %"
    }


}

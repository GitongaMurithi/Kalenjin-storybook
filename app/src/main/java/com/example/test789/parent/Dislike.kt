package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityDislikeBinding
import com.example.test789.models.Review
import com.google.firebase.database.FirebaseDatabase

class Dislike : AppCompatActivity() {

    private lateinit var binding : ActivityDislikeBinding
    private lateinit var reviewId : String
    private lateinit var childId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDislikeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val bookId = intent.getStringExtra("id").toString()
        reviewId = FirebaseDatabase.getInstance().getReference("Reviews").push().key!!
        childId = intent.getStringExtra("childId").toString()


        binding.submitReason.setOnClickListener {
            if (binding.myReason.text.toString().isEmpty()) {
                binding.myReason.error = "Please give your reason"
                binding.myReason.requestFocus()
            } else {
                binding.submitLoading.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().getReference("Reviews/$bookId/$reviewId")
                    .setValue(
                        Review(
                        bookId = bookId,
                        review = binding.myReason.text.toString()
                    )
                    )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.submitLoading.visibility = View.GONE
                            binding.myReason.text.clear()
                            Toast.makeText(this , "Submitted" , Toast.LENGTH_SHORT).show()
                            navigateUpTo(Intent(this , ParentDashboard::class.java))
                        } else {
                            binding.submitLoading.visibility = View.GONE
                            Toast.makeText(this , task.exception?.localizedMessage , Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
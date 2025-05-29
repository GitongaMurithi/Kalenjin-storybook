package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.AttemptedAdapter
import com.example.test789.databinding.ActivityAttemptedBooksBinding
import com.example.test789.models.AttemptedBooks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttemptedBooks : AppCompatActivity() {

    private lateinit var binding: ActivityAttemptedBooksBinding
    private lateinit var userId: String
    private lateinit var childId: String
    private lateinit var attemptedBooks: ArrayList<AttemptedBooks>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttemptedBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)


        attemptedBooks = ArrayList()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        childId = intent.getStringExtra("id").toString()

        binding.back.setOnClickListener {
            finish()
        }
        binding.attemptedRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.attemptedRecycler.layoutManager = layoutManager

        val database = FirebaseDatabase.getInstance().reference
        val attemptedBooksRef = database.child("Children/$userId/$childId").child("attemptedBooks")

        attemptedBooksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                attemptedBooks.clear()
                snapshot.children.forEach { book ->
                    attemptedBooks.add(book.getValue(AttemptedBooks::class.java)!!)
                    binding.attemptedRecycler.adapter = AttemptedAdapter(
                        stories = attemptedBooks
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AttemptedBooks, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}
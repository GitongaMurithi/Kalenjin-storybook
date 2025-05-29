package com.example.test789.parent

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.CompletedBooksAdapter
import com.example.test789.databinding.ActivityReadBooksBinding
import com.example.test789.models.CompletedBooks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReadBooks : AppCompatActivity() {

    private lateinit var binding: ActivityReadBooksBinding
    private lateinit var userId: String
    private lateinit var childId: String
    private lateinit var completedBooks: ArrayList<CompletedBooks>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)


        completedBooks = ArrayList()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        childId = intent.getStringExtra("id").toString()

        binding.back.setOnClickListener { finish() }
        binding.readRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.readRecycler.layoutManager = layoutManager

        val database = FirebaseDatabase.getInstance().reference
        val readBooksRef = database.child("Children/$userId/$childId").child("completedBooks")


        readBooksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedBooks.clear()
                snapshot.children.forEach { completedBook ->
                    completedBooks.add(completedBook.getValue(CompletedBooks::class.java)!!)
                    binding.readRecycler.adapter = CompletedBooksAdapter(
                        stories = completedBooks
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadBooks, error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }
}
package com.example.test789.parent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityChildProgressBinding
import com.example.test789.models.Child
import com.example.test789.models.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChildProgress : AppCompatActivity() {

    private lateinit var binding : ActivityChildProgressBinding
    private var stories: Int = 0


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

         val child = intent.getSerializableExtra("child") as? Child

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().getReference("Children/$userId")
            .child(child!!.id).child("attemptedBooks")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.attemptedV.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        FirebaseDatabase.getInstance().getReference("Children/$userId")
            .child(child.id).child("completedBooks")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.completedV.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding.childName.text = child.name

        FirebaseDatabase.getInstance().getReference("Stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { story ->
                        if (story.getValue(Story::class.java)!!.age <= child.age) {
                            stories += 1
                            binding.totalV.text = stories.toString()
                        } else {
                            binding.totalV.text = stories.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.info2.setOnClickListener {
            showDialogue()
        }

        binding.info0.setOnClickListener {
            startActivity(
                Intent(this, AttemptedBooks::class.java)
                    .putExtra("id", child.id)
            )
        }

        binding.info1.setOnClickListener {
            startActivity(
                Intent(this, ReadBooks::class.java)
                    .putExtra("id", child.id)
            )
        }
    }

    private fun showDialogue() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Info")
            .setMessage("Accessible books depends on the age of the child. Consider updating age accordingly.")
            .setPositiveButton(
                "Okay"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }
}
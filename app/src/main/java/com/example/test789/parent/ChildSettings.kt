package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityChildSettingsBinding
import com.example.test789.models.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChildSettings : AppCompatActivity() {

    private lateinit var binding: ActivityChildSettingsBinding
    private lateinit var uid: String
    private lateinit var child: Child

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        child = intent.getSerializableExtra("child") as Child
        uid = FirebaseAuth.getInstance().currentUser!!.uid

        binding.name.setText(child.name)
//        binding.childAge.setText(child.age)

        binding.buttonUpdate.setOnClickListener { validate() }
    }

    private fun validate() {
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "This is a required field."
            binding.name.requestFocus()
            return
        }

        if (binding.childAge.text.toString().isEmpty()) {
            binding.childAge.error = "This is a required field."
            binding.childAge.requestFocus()
            return
        }

        update()
    }

    private fun update() {
        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference("Children/$uid/${child.id}/name")
            .setValue(binding.name.text.toString())

        FirebaseDatabase.getInstance().getReference("Children/$uid/${child.id}/age")
            .setValue(binding.childAge.text.toString().toInt())

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    binding.name.text.clear()
                    binding.childAge.text.clear()

                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this@ChildSettings, ParentDashboard::class.java
                        )
                    )
                } else {
                    Toast.makeText(this, task.exception!!.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}
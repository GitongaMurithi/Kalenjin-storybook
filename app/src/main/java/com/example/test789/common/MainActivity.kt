package com.example.test789.common

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.R
import com.example.test789.databinding.ActivityMainBinding
import com.example.test789.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val users = arrayOf(" ", "Parent", "Author")
    private val dialects = arrayOf("Dialect", "Keiyo", "Nandi", "Tugen")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button1.setOnClickListener {
            startActivity(Intent(this@MainActivity, Login::class.java))
        }

        binding.registerBtn.setOnClickListener {
            registerUser()
        }

        auth = FirebaseAuth.getInstance()

        binding.dialectSpinner.onItemSelectedListener = this
        val dialectAdapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, dialects)
        dialectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dialectSpinner.adapter = dialectAdapter

        binding.spinner.onItemSelectedListener = this
        val adapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, users)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

    }

    private fun registerUser() {
        if (binding.edittext17.text.toString().trim().isEmpty()) {
            binding.edittext17.error = "This is a required field!"
            binding.edittext17.requestFocus()
            return
        }
        if (binding.edittext18.text.toString().isEmpty()) {
            binding.edittext18.error = "This is a required field!"
            binding.edittext18.requestFocus()
            return
        }
        if (binding.edittext15.text.toString().trim().isEmpty()) {
            binding.edittext15.error = "This is a required field!"
            binding.edittext15.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.edittext15.text.toString().trim()).matches()) {
            binding.edittext15.error = "Enter a valid email address!"
            binding.edittext15.requestFocus()
            return
        }
        if (binding.edittext16.text.toString().trim().isEmpty()) {
            binding.edittext16.error = "This is a required field!"
            binding.edittext16.requestFocus()
            return
        }

        if (binding.dialectValue.text.toString().trim() == "Dialect") {
            Toast.makeText(this, "Select dialect", Toast.LENGTH_LONG).show()
            return
        }
        if (binding.selectedText.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Select user", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar6.visibility = View.VISIBLE

        createUserWithEmailAndPassword()
    }

    private fun createUserWithEmailAndPassword() {
        auth.createUserWithEmailAndPassword(
            binding.edittext15.text.toString(),
            binding.edittext16.text.toString()
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(
                        name = binding.edittext17.text.toString(),
                        email = binding.edittext15.text.toString(),
                        phone = binding.edittext18.text.toString(),
                        image = "",
                        dialect = binding.dialectValue.text.toString(),
                        password = binding.edittext16.text.toString(),
                        role = binding.selectedText.text.toString()
                    )
                    FirebaseDatabase.getInstance().getReference("App users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(user)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                startActivity(Intent(this@MainActivity, Login::class.java))
                                binding.progressBar6.visibility = View.GONE
                                binding.edittext15.text.clear()
                                binding.edittext17.text.clear()
                                binding.edittext18.text.clear()
                                binding.edittext16.text.clear()
                                binding.dialectValue.text = null
                                binding.edittext16.clearFocus()
                                binding.spinner.setSelection(0)
                                binding.dialectSpinner.setSelection(0)


                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    task1.exception?.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.progressBar6.visibility = View.GONE
                            }
                        }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    binding.progressBar6.visibility = View.GONE
                }
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selection = parent?.getItemAtPosition(position).toString()
        when (parent!!.id) {
            R.id.dialect_spinner -> {
                binding.dialectValue.text = selection
            }

            R.id.spinner -> {
                if (selection == "Parent") {
                    binding.selectedText.text = selection
                    binding.selectedText.visibility = View.VISIBLE
                    return
                }

                if (selection == "Author") {
                    binding.selectedText.text = selection
                    binding.selectedText.visibility = View.VISIBLE
                    return
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
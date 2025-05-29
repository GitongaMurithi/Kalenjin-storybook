package com.example.test789.common

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test789.R
import com.example.test789.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding : ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        binding.button9.setOnClickListener {
            val email = binding.edittext17.text.toString().trim()

            if (email.isEmpty()) {
                binding.edittext17.error = "This is a required field!"
                binding.edittext17.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edittext17.error = "Enter a valid email address!"
                binding.edittext17.requestFocus()
                return@setOnClickListener
            }
            binding.progressBar7.visibility = View.VISIBLE

            auth.sendPasswordResetEmail(email).addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    if (task.isSuccessful) {
                        binding.progressBar7.visibility = View.GONE
                        Toast.makeText(
                            this@ForgotPassword,
                            "Check your email to reset password",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@ForgotPassword, Login::class.java))
                        binding.edittext17.text.clear()
                    } else {
                        binding.progressBar7.visibility = View.GONE
                        Toast.makeText(
                            this@ForgotPassword,
                            "Failed to reset the password!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
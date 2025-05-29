package com.example.test789.parent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityEditParentBinding
import com.example.test789.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage

class EditParent : AppCompatActivity() {

    private lateinit var binding: ActivityEditParentBinding
    private var selectedImageUri: Uri? = null
    private lateinit var uid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditParentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        binding.dpHolder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        FirebaseDatabase.getInstance().getReference("App users/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.name.setText(snapshot.getValue(User::class.java)!!.name)
                    binding.phone.setText(snapshot.getValue(User::class.java)!!.phone)
                    binding.email.setText(snapshot.getValue(User::class.java)!!.email)
                    binding.dialect.setText(snapshot.getValue(User::class.java)!!.dialect)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditParent, error.message, Toast.LENGTH_LONG).show()
                }

            })
        binding.update.setOnClickListener { validateFields() }
    }

    private fun validateFields() {
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "This is a required field!"
            binding.name.requestFocus()
            return
        }

        if (binding.phone.text.toString().isEmpty()) {
            binding.phone.error = "This is a required field!"
            binding.phone.requestFocus()
            return
        }

        if (binding.email.text.toString().isEmpty()) {
            binding.email.error = "This is a required field!"
            binding.email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.error = "Enter a valid email address!"
            binding.email.requestFocus()
            return
        }

        binding.progress.visibility = View.VISIBLE
        uploadData()
    }

    private fun uploadData() {
        FirebaseDatabase.getInstance().getReference("App users/$uid/name")
            .setValue(binding.name.text.toString())

        FirebaseDatabase.getInstance().getReference("App users/$uid/phone")
            .setValue(binding.name.text.toString())

        FirebaseDatabase.getInstance().getReference("App users/$uid/email")
            .setValue(binding.name.text.toString())

        FirebaseDatabase.getInstance().getReference("App users/$uid/dialect")
            .setValue(binding.name.text.toString())

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadImage()
                    binding.name.text.clear()
                    binding.name.text.clear()
                    binding.phone.text.clear()
                    binding.email.text.clear()
                    binding.dialect.text.clear()
                    binding.dialect.clearFocus()
                    binding.progress.visibility = View.GONE
                    startActivity(
                        Intent(
                            this, ParentDashboard::class.java
                        )

                    )
                    finish()
                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_LONG).show()
                } else {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this, task.exception!!.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                binding.dp.setImageURI(selectedImageUri)

            }
        }

    private fun uploadImage() {
        if (selectedImageUri == null) {
            return
        } else {
            selectedImageUri.let {
                val storageReference = Firebase.storage.reference
                val imagesReference =
                    storageReference.child("images/${selectedImageUri?.lastPathSegment}")
                val uploadTask = imagesReference.putFile(selectedImageUri!!)

                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imagesReference.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            FirebaseDatabase.getInstance()
                                .getReference("App users/${FirebaseAuth.getInstance().currentUser!!.uid}")
                                .child("image")
                                .setValue(imageUrl)
                        }.addOnFailureListener {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, task.exception!!.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    }
}
package com.example.test789.parent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.databinding.ActivityAddChildBinding
import com.example.test789.models.Child
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage

class AddChild : AppCompatActivity() {

    private lateinit var binding : ActivityAddChildBinding

    private var selectedImageUri: Uri? = null
    private lateinit var id : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)


        id = FirebaseDatabase.getInstance().getReference("Children/${FirebaseAuth.getInstance().currentUser!!.uid}")
            .push().key!!

        binding.dpHolder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        binding.update.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "This is a required field!"
            binding.name.requestFocus()
            return
        }

        if (binding.childAge.text.toString().isEmpty()) {
            binding.childAge.error = "This is a required field!"
            binding.childAge.requestFocus()
            return
        }

        binding.progress.visibility = View.VISIBLE
        uploadData()
    }

    private fun uploadData() {
        FirebaseDatabase.getInstance().getReference("Children/${FirebaseAuth.getInstance().currentUser!!.uid}").child(id)
            .setValue(
                Child(
                    name = binding.name.text.toString(),
                    age = binding.childAge.text.toString().toInt(),
                    image = "",
                    id = id
                )
            )
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    uploadImage()
                    binding.name.text.clear()
                    binding.childAge.text.clear()
                    binding.progress.visibility = View.GONE
                    startActivity(
                        Intent(
                            this@AddChild,
                            ParentDashboard::class.java
                        )
                    )
                    Toast.makeText(this , "Child added successfully" , Toast.LENGTH_LONG).show()
                } else {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this , task.exception!!.localizedMessage , Toast.LENGTH_LONG).show()
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
                val imagesReference = storageReference.child("images/${selectedImageUri?.lastPathSegment}")
                val uploadTask = imagesReference.putFile(selectedImageUri!!)

                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imagesReference.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            FirebaseDatabase.getInstance().getReference("Children/${FirebaseAuth.getInstance().currentUser!!.uid}")
                                .child(id)
                                .child("image")
                                .setValue(imageUrl)
                        } .addOnFailureListener {
                            Toast.makeText(this , it.localizedMessage , Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this , task.exception!!.localizedMessage , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}
package com.example.test789.author

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.test789.R
import com.example.test789.common.Login
import com.example.test789.databinding.ActivityEditStoryBinding
import com.example.test789.models.Story
import com.example.test789.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class EditStory : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityEditStoryBinding


    private lateinit var reference: DatabaseReference
    private lateinit var storyId: String
    private lateinit var author: String
    private lateinit var id: String
    private var selectedImageUri: Uri? = null
    private var fileUri: Uri? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fileUri = it
                val name = getFileName(it)
                binding.contentValue.text = name
            }
        }

    private val categories =
        arrayOf("All", "Trickery", "Humour", "Poetry", "Musical", "Sports", "Beauty")

    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra("id").toString()
//        retrieveInfo(id = id)

        reference = FirebaseDatabase.getInstance().reference
        storyId = reference.child("Stories").push().key!!

        binding.spinner.onItemSelectedListener = this
        val adapter = ArrayAdapter(
            this@EditStory,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        binding.logout.setOnClickListener { showDialogue() }

        reference.child("App users/${FirebaseAuth.getInstance().currentUser!!.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    author = snapshot.getValue(User::class.java)!!.name
                    binding.greetings.text =
                        getString(R.string.author_greetings, author.split(" ")[0])
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditStory, error.message, Toast.LENGTH_LONG).show()
                }

            })

        binding.thumbnailHolder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        binding.select.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }

        binding.upload.setOnClickListener {
            verifyInput()
        }

    }

    private fun verifyInput() {
        if (binding.editTitle.text.toString().isEmpty()) {
            binding.editTitle.error = "This is a required field!"
            binding.editTitle.requestFocus()
            return
        }

        if (binding.editPages.text.toString().isEmpty()) {
            binding.editPages.error = "This is a required field!"
            binding.editPages.requestFocus()
            return
        }

        if (binding.editAge.text.toString().isEmpty()) {
            binding.editAge.error = "This is a required field!"
            binding.editAge.requestFocus()
            return
        }

        if (binding.editDescription.text.toString().isEmpty()) {
            binding.editDescription.error = "This is a required field!"
            binding.editDescription.requestFocus()
            return
        }

        if (binding.selectedText.text.toString().trim().isEmpty()) {
            Toast.makeText(this@EditStory, "Select category", Toast.LENGTH_LONG).show()
            return
        }

        if (binding.contentValue.text.toString().isEmpty()) {
            Toast.makeText(this@EditStory, "Select file to upload", Toast.LENGTH_LONG).show()
            return
        }
        uploadDetails()
    }

    private fun uploadDetails() {
        binding.progress.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference("Stories/$id")
            .setValue(
                Story(
                    id = storyId,
                    authorId = FirebaseAuth.getInstance().currentUser!!.uid,
                    author = author,
                    title = binding.editTitle.text.toString(),
                    category = category,
                    age = binding.editAge.text.toString().toIntOrNull() ?: 1,
                    pages = binding.editPages.text.toString().toIntOrNull() ?: 1,
                    description = binding.editDescription.text.toString()
                )
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    uploadThumbnail()

                    fileUri?.let {
                        uploadContent(it)
                    }

                } else {
                    binding.progress.visibility = View.GONE

                    Toast.makeText(
                        this,
                        task.exception!!.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun uploadContent(uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = getFileName(uri)
        val fileRef = storageReference.child("contents/$fileName")
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveToDatabase(fileName, downloadUrl.toString())
                    binding.editTitle.text.clear()
                    binding.editAge.text.clear()
                    binding.editPages.text.clear()
                    binding.editDescription.text.clear()
                    binding.editDescription.clearFocus()
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@EditStory, "Edited successfully", Toast.LENGTH_LONG)
                        .show()
                    startActivity(
                        Intent(
                            this , AuthorDashboard::class.java
                        )
                    )
                    finish()
                    overridePendingTransition(0,0)
                }
            }.addOnFailureListener {
                Toast.makeText(this@EditStory, it.message, Toast.LENGTH_LONG).show()
            }

    }

    private fun getFileName(uri: Uri): String {
        var name = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun saveToDatabase(fileName: String, fileUrl: String) {
        val fileData = mapOf(
            "name" to fileName,
            "url" to fileUrl
        )
        FirebaseDatabase.getInstance().getReference("Stories/$storyId")
            .child("content").setValue(fileData)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                binding.thumbnailHolder.setImageURI(selectedImageUri)

            }
        }

    private fun uploadThumbnail() {
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
                            FirebaseDatabase.getInstance().getReference("Stories/$storyId")
                                .child("image")
                                .setValue(imageUrl)
                        }.addOnFailureListener {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this@EditStory,
                            task.exception!!.localizedMessage,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        binding.selectedText.text = binding.spinner.selectedItem.toString()
        binding.selectedText.visibility = View.VISIBLE
        category = binding.spinner.selectedItem.toString()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this@EditStory, "Select category", Toast.LENGTH_SHORT).show()
    }

    private fun showDialogue() {
        val alertDialog = AlertDialog.Builder(this@EditStory)
        alertDialog.setTitle("Logout")
            .setMessage("Sure to logout?")
            .setPositiveButton(
                "Yes"
            ) { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent (
            this@EditStory , Login::class.java
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun retrieveInfo(id: String) {
        FirebaseDatabase.getInstance().getReference("Stories/$id")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.editTitle.setText(snapshot.getValue(Story::class.java)!!.title)
                    binding.editAge.setText(snapshot.getValue(Story::class.java)!!.age)
                    binding.editPages.setText(snapshot.getValue(Story::class.java)!!.pages)
                    binding.editDescription.setText(snapshot.getValue(Story::class.java)!!.description)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
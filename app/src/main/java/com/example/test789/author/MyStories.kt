package com.example.test789.author

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.R
import com.example.test789.adapters.AuthorStoriesAdapter
import com.example.test789.databinding.FragmentMyStoriesBinding
import com.example.test789.models.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyStories : Fragment() {

    private var binding: FragmentMyStoriesBinding? = null
    private val _binding get() = binding!!

    private lateinit var stories: ArrayList<Story>
    private lateinit var onAction: AuthorStoriesAdapter.OnAction
    private lateinit var authorId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyStoriesBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stories = ArrayList()
        authorId = FirebaseAuth.getInstance().currentUser!!.uid

        onAction = object : AuthorStoriesAdapter.OnAction {
            override fun onEditClick(position: Int) {
                val story = stories[position]

                requireContext().startActivity(
                    Intent(
                        requireContext(), EditStory::class.java
                    )
                        .putExtra("id", story.id)
                )


            }

            override fun onDeleteClicked(position: Int) {
                val story = stories[position]
                showDialogue(
                    id = story.id
                )
            }

            override fun onStoryClicked(position: Int) {
                val story = stories[position]
                requireContext().startActivity(
                    Intent(
                        requireContext(), Details::class.java
                    )
                        .putExtra("id", story.id)
                )
            }

        }

        binding!!.myStories.hasFixedSize()
        val manager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding!!.myStories.layoutManager = manager

        FirebaseDatabase.getInstance().getReference("Stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { story ->
                        val id = story.getValue(Story::class.java)!!.authorId
                        if (id == authorId) {
                            stories.add(story.getValue(Story::class.java)!!)
                            binding!!.empty.visibility = View.GONE
                            binding!!.myStories.adapter = AuthorStoriesAdapter(
                                stories = stories,
                                onAction = onAction
                            )
                        }
                    }
                    if (stories.isEmpty()) {
                        binding!!.empty.visibility = View.VISIBLE
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                }

            })
    }

    private fun showDialogue(id: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Delete story")
            .setMessage("Deleting can not be undone!")
            .setPositiveButton(
                "I understand"
            ) { dialog, _ ->
                deleteStory(id = id)
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun deleteStory(id: String) {
        FirebaseDatabase.getInstance().getReference("Stories/$id")
            .removeValue()
            .addOnSuccessListener {
                findNavController().run {
                    popBackStack()
                    navigate(R.id.myStories)
                }
            }
    }
}
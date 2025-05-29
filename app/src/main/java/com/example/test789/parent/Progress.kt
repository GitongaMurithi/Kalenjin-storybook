package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.ProgressAdapter
import com.example.test789.databinding.FragmentProgressBinding
import com.example.test789.models.Child
import com.example.test789.models.Feedback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Progress : Fragment() {

    private var binding: FragmentProgressBinding? = null
    private val _binding get() = binding!!
    private lateinit var onChildClick: ProgressAdapter.OnChildClick
    private lateinit var children: ArrayList<Child>
    private lateinit var child: Child
    private lateinit var userId: String



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProgressBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        children = ArrayList()
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        binding!!.back.setOnClickListener { findNavController().popBackStack() }

        binding!!.profiles.setHasFixedSize(true)
        val manager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding!!.profiles.layoutManager = manager

        onChildClick = object : ProgressAdapter.OnChildClick {
            override fun onChildClicked(position: Int) {
                child = children[position]
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        ChildProgress::class.java
                    )
                        .putExtra("child", child)
                )

            }


        }

        FirebaseDatabase.getInstance()
            .getReference("Children/${FirebaseAuth.getInstance().currentUser!!.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    children.clear()
                    if (snapshot.exists()) {
                        snapshot.children.forEach { child ->
                            children.add(child.getValue(Child::class.java)!!)
                            binding!!.profiles.adapter = ProgressAdapter(
                                context = requireContext(),
                                children = children,
                                onChildClick = onChildClick
                            )
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Add child to see progress",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding!!.submitReason.setOnClickListener {

            if (binding!!.myReason.text.toString().isEmpty()) {
                binding!!.myReason.error = "Please give your feedback"
                binding!!.myReason.requestFocus()
            } else {
                binding!!.submitLoading.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().getReference("Feedbacks/$userId")
                    .setValue(
                        Feedback(
                            feedback = binding!!.myReason.text.toString()
                        )
                    )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding!!.submitLoading.visibility = View.GONE
                            binding!!.myReason.text.clear()
                            binding!!.myReason.clearFocus()
                            Toast.makeText(requireContext(), "Submitted", Toast.LENGTH_SHORT).show()
                        } else {
                            binding!!.submitLoading.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                task.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

    }

}


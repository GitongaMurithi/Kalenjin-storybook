package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.ChildProfilesAdapter
import com.example.test789.databinding.FragmentParentHomeBinding
import com.example.test789.models.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ParentHomeFragment : Fragment() {

    private var binding: FragmentParentHomeBinding? = null
    private val _binding get() = binding!!

    private lateinit var children: ArrayList<Child>
    private lateinit var onChildClick: ChildProfilesAdapter.OnChildClick

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentParentHomeBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        children = ArrayList()

        binding!!.back.setOnClickListener { findNavController().popBackStack() }

        binding!!.addButton.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(), AddChild::class.java
                )
            )
        }

        binding!!.profiles.setHasFixedSize(true)
        val manager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding!!.profiles.layoutManager = manager

        onChildClick = object : ChildProfilesAdapter.OnChildClick {
            override fun onChildClicked(position: Int) {
                val currentChild = children[position]
                startActivity(
                    Intent(
                        requireContext(), ViewStories::class.java
                    )
                        .putExtra("age", currentChild.age)
                        .putExtra("name", currentChild.name)
                        .putExtra("childId", currentChild.id)
                        .putExtra("parentId", FirebaseAuth.getInstance().currentUser!!.uid)
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
                            binding!!.profilesHolder.visibility = View.VISIBLE
                            children.add(child.getValue(Child::class.java)!!)
                            binding!!.profiles.adapter = ChildProfilesAdapter(
                                context = requireContext(),
                                children = children,
                                onChildClick = onChildClick
                            )
                        }

                    } else {
                        binding!!.profilesHolder.visibility = View.GONE
                        binding!!.empty.visibility = View.VISIBLE
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}
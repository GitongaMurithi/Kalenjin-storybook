package com.example.test789.parent

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
import com.bumptech.glide.Glide
import com.example.test789.R
import com.example.test789.adapters.ProgressAdapter
import com.example.test789.common.Login
import com.example.test789.databinding.FragmentProfileBinding
import com.example.test789.models.Child
import com.example.test789.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Profile : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val _binding get() = binding!!

    private lateinit var onChildClick: ProgressAdapter.OnChildClick
    private lateinit var children: ArrayList<Child>
    private lateinit var child: Child
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        children = ArrayList()
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        binding!!.back.setOnClickListener { findNavController().popBackStack() }

        binding!!.profiles.setHasFixedSize(true)
        val manager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding!!.profiles.layoutManager = manager

        binding!!.edit.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(), EditParent::class.java
                )
            )
        }

        binding!!.logout.setOnClickListener {
            showDialogue()
        }

        FirebaseDatabase.getInstance().getReference("App users/${FirebaseAuth.getInstance().currentUser!!.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding!!.phone.text = snapshot.getValue(User::class.java)!!.phone
                    binding!!.email.text = snapshot.getValue(User::class.java)!!.email
                    binding!!.language.text = snapshot.getValue(User::class.java)!!.dialect
                    binding!!.user.text = snapshot.getValue(User::class.java)!!.name

                    Glide.with(requireContext()).load(snapshot.getValue(User::class.java)!!.image).placeholder(
                        R.drawable.baseline_account_circle_24).error(
                        R.drawable.baseline_account_circle_24
                    ).into(binding!!.dp)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        onChildClick = object : ProgressAdapter.OnChildClick {
            override fun onChildClicked(position: Int) {
                child = children[position]
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        ChildSettings::class.java
                    )
                        .putExtra("child", child)
                )

            }


        }

        FirebaseDatabase.getInstance()
            .getReference("Children/$uid")
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
                            "Add child to see update",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun showDialogue() {
        val alertDialog = AlertDialog.Builder(requireContext())
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
            requireContext() , Login::class.java
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
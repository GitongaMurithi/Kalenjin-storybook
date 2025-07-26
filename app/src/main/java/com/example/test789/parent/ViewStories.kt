package com.example.test789.parent

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test789.adapters.CategoryAdapter
import com.example.test789.adapters.ParentHomeAdapter
import com.example.test789.databinding.ActivityViewStoriesBinding
import com.example.test789.models.Category
import com.example.test789.models.Child
import com.example.test789.models.Story
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewStories : AppCompatActivity() {

    private lateinit var binding: ActivityViewStoriesBinding

    private val allStories = mutableListOf<Story>()
    private val filteredStories = mutableListOf<Story>()
    private lateinit var onStoryClicked: ParentHomeAdapter.OnStoryClicked
    private lateinit var onCategoryClick: CategoryAdapter.OnCategoryClick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val childId = intent.getStringExtra("childId").toString()
        val parentId = intent.getStringExtra("parentId").toString()

        binding.search.setOnClickListener {
            binding.search.visibility = View.GONE
            binding.text.visibility = View.GONE
            binding.searchView.visibility = View.VISIBLE
        }

        val categories = listOf(
            Category(category = "All"),
            Category(category = "Trickery"),
            Category(category = "Humour"),
            Category(category = "Poetry"),
            Category(category = "Musical"),
            Category(category = "Sports"),
            Category(category = "Beauty"),
        )

        onCategoryClick = object : CategoryAdapter.OnCategoryClick {
            override fun onCategoryClicked(position: Int) {
                val currentCategory = categories[position]
                val selectedCategory = currentCategory.category
                filterByCategory(selectedCategory)
            }

        }

        binding.categoriesRecycler.setHasFixedSize(true)
        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecycler.layoutManager = manager
        binding.categoriesRecycler.adapter = CategoryAdapter(categories, onCategoryClick)

        onStoryClicked = object : ParentHomeAdapter.OnStoryClicked {
            override fun onStoryClicked(position: Int) {
                val currentStory = filteredStories[position]
                startActivity(
                    Intent(this@ViewStories, BookDetails::class.java)
                        .putExtra("authorId", currentStory.authorId)
                        .putExtra("id", currentStory.id)
                        .putExtra("childId", childId)
                        .putExtra("title", currentStory.title)

                )
            }

        }

        binding.searchView.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchProducts(newText)
                return true
            }

        })


        binding.productsView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        binding.productsView.layoutManager = layoutManager


        FirebaseDatabase.getInstance().getReference("Children/$parentId/$childId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val age = snapshot.getValue(Child::class.java)!!.age

                    FirebaseDatabase.getInstance().getReference("Stories")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                allStories.clear()
                                filteredStories.clear()
                                snapshot.children.forEach { dataSnapshot ->
                                    if (dataSnapshot.getValue(Story::class.java)!!.age <= age.toString()
                                            .toInt()
                                    ) {
                                        allStories.add(dataSnapshot!!.getValue(Story::class.java)!!)
                                    }
                                }
                                filteredStories.addAll(allStories)
                                updateUI()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun updateUI() {
        binding.loading.visibility = View.GONE
        binding.productsView.visibility = if (filteredStories.isNotEmpty()) View.VISIBLE else View.GONE
        binding.empty.visibility = if (filteredStories.isEmpty()) View.VISIBLE else View.GONE

        binding.productsView.adapter = ParentHomeAdapter(
            context = this,
            onStoryClicked = onStoryClicked,
            stories = filteredStories
        )
    }

    private fun searchProducts(newText: String) {
        filteredStories.clear()
        if (newText.isEmpty()) {
            filteredStories.addAll(allStories)
            binding.productsView.adapter = ParentHomeAdapter(
                context = this,
                stories = filteredStories,
                onStoryClicked = onStoryClicked
            )
        } else {
            val filteredList = allStories.filter { story ->
                story.author.contains(newText, ignoreCase = true) ||
                        story.title.contains(newText, ignoreCase = true)
            }
            filteredStories.addAll(filteredList)
            binding.productsView.adapter = ParentHomeAdapter(
                context = this,
                stories = filteredStories,
                onStoryClicked = onStoryClicked
            )
        }
    }

    private fun filterByCategory(selectedCategory: String) {
        filteredStories.clear()
        if (selectedCategory == "All") {
            filteredStories.addAll(allStories)
            binding.productsView.adapter = ParentHomeAdapter(
                context = this,
                onStoryClicked = onStoryClicked,
                stories = filteredStories
            )
        } else {
            filteredStories.addAll(
                allStories.filter { it.category == selectedCategory }
            )
        }

        updateUI()
    }
}

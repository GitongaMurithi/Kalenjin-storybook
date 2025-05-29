package com.example.test789.models

data class Story(
    val id: String = "",
    val authorId: String = "",
    val image: String = "",
    val author: String = "Author",
    val title: String = "Title",
    val category: String = "",
    val description: String = "",
    val content: Document = Document(),
    val pages: Int = 0,
    val age: Int = 0,
    var read: Int = 0,
    var liked: Int = 0,
    var disliked: Int = 0,
    var readBy: MutableMap<String, Boolean>? = mutableMapOf(),
    var likedBy: MutableMap<String, Boolean>? = mutableMapOf(),
    var dislikedBy: MutableMap<String, Boolean>? = mutableMapOf()
)

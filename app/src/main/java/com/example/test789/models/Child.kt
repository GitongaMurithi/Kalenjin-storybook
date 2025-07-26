package com.example.test789.models

import java.io.Serializable

data class Child(
    val name : String = "",
    val age : Int = 0,
    val attemptedBooks: Map<String, Book> = emptyMap(),
    val completedBooks: Map<String, Book> = emptyMap(),
    val image : String = "",
    val id : String = ""
) : Serializable

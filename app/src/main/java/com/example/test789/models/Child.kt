package com.example.test789.models

import java.io.Serializable

data class Child(
    val name : String = "",
    val age : Int = 0,
    var attemptedBooks : AttemptedBooks = AttemptedBooks(),
    var completedBooks : CompletedBooks = CompletedBooks(),
    val image : String = "",
    val id : String = ""
) : Serializable

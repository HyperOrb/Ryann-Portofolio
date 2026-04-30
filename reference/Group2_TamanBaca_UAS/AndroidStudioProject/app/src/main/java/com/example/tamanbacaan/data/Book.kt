package com.example.tamanbacaan.data

data class Book(
    val id: String = "",
    val code: String = "",
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val publishYear: String = "",
    val isbn: String = "",
    val pages: String = "",
    val language: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val category: String = "",
    val rating: Float = 0f,
    val isBorrowed: Boolean = false
)

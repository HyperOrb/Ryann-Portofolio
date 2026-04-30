package com.example.tamanbacaan.data

data class Borrow(
    val id: String = "",
    val uid: String = "",
    val bookId: String = "",
    val startDate: Long = 0L,
    val dueDate: Long = 0L,
    val returned: Boolean = false
)

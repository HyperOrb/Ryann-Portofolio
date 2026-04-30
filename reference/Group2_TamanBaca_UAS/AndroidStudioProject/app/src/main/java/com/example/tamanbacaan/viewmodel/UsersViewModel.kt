package com.example.tamanbacaan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamanbacaan.data.AppUser
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Borrow
import com.example.tamanbacaan.data.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to hold the combined information for the UI
data class UserWithBorrows(
    val user: AppUser,
    val borrowedBooks: List<Pair<Borrow, Book>>
)

class UsersViewModel(private val repo: Repo = Repo()) : ViewModel() {

    private val _usersWithBorrows = MutableStateFlow<List<UserWithBorrows>>(emptyList())
    val usersWithBorrows = _usersWithBorrows.asStateFlow()

    init {
        loadUsersAndBorrows()
    }

    private fun loadUsersAndBorrows() {
        viewModelScope.launch {
            try {
                // Fetch all users and all books once to be efficient
                val users = repo.listUsers()
                val booksMap = repo.listBooks().associateBy { it.id }

                val resultList = mutableListOf<UserWithBorrows>()

                // For each user, find their borrowed books
                for (user in users) {
                    val userBorrows = repo.borrowsByUser(user.uid)
                    val detailedBorrows = mutableListOf<Pair<Borrow, Book>>()

                    for (borrow in userBorrows) {
                        // Find the book details from the map
                        booksMap[borrow.bookId]?.let { book ->
                            detailedBorrows.add(Pair(borrow, book))
                        }
                    }
                    resultList.add(UserWithBorrows(user, detailedBorrows))
                }
                _usersWithBorrows.value = resultList
            } catch (e: Exception) {
                // Handle error, maybe expose an error state
                _usersWithBorrows.value = emptyList()
            }
        }
    }
}
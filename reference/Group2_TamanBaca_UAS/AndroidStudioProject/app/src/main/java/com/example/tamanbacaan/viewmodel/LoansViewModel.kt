package com.example.tamanbacaan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Borrow
import com.example.tamanbacaan.data.Repo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BorrowedBookDetails(
    val borrow: Borrow,
    val book: Book
)

class LoansViewModel(private val repo: Repo = Repo()) : ViewModel() {

    private val _activeLoans = MutableStateFlow<List<BorrowedBookDetails>>(emptyList())
    val activeLoans = _activeLoans.asStateFlow()

    private val _historyLoans = MutableStateFlow<List<BorrowedBookDetails>>(emptyList())
    val historyLoans = _historyLoans.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadBorrowedBooks()
    }

    private fun loadBorrowedBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = auth.currentUser
            if (user != null) {
                try {
                    // Combine both flows to listen real-time changes for borrows AND books
                    combine(
                        repo.borrowsFlow(user.uid),
                        repo.booksFlow()
                    ) { userBorrows, books ->
                        val booksMap = books.associateBy { it.id }

                        val detailedBorrows = userBorrows.mapNotNull { borrow ->
                            booksMap[borrow.bookId]?.let { book ->
                                BorrowedBookDetails(borrow, book)
                            }
                        }

                        val (active, history) = detailedBorrows.partition { !it.borrow.returned }
                        Pair(active, history)
                    }.collect { (active, history) ->
                        _activeLoans.value = active
                        _historyLoans.value = history
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                }
            } else {
                _activeLoans.value = emptyList()
                _historyLoans.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun returnBook(borrowId: String, bookId: String) {
        viewModelScope.launch {
            try {
                repo.returnBook(borrowId, bookId)
                // UI will update automatically thanks to the flow collector in init
            } catch (e: Exception) {
                // Handle or log error
            }
        }
    }
}

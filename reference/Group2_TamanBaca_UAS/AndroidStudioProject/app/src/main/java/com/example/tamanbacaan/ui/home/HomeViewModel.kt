package com.example.tamanbacaan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repo = Repo()) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _featured = MutableStateFlow<List<Book>>(emptyList())
    val featured: StateFlow<List<Book>> = _featured.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _totalDipinjam = MutableStateFlow(0)
    val totalDipinjam: StateFlow<Int> = _totalDipinjam.asStateFlow()

    private val _totalTersedia = MutableStateFlow(0)
    val totalTersedia: StateFlow<Int> = _totalTersedia.asStateFlow()

    init {
        viewModelScope.launch {
            repo.booksFlow().collectLatest { allBooks ->
                _books.value = allBooks
                _featured.value = allBooks.take(5)
                _categories.value = allBooks.map { it.category }.distinct().filter { it.isNotBlank() }
                _totalDipinjam.value = allBooks.count { it.isBorrowed }
                _totalTersedia.value = allBooks.count { !it.isBorrowed }
            }
        }
    }

    fun borrowBook(bookId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.borrowBook(bookId, userId)
            } catch (e: Exception) {
                // Handle or log the exception, e.g., show a toast
            }
        }
    }
}
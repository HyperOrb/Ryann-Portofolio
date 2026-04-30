package com.example.tamanbacaan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogViewModel(private val repo: Repo = Repo()) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    init {
        viewModelScope.launch {
            repo.booksFlow().collectLatest { allBooks ->
                _books.value = allBooks
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
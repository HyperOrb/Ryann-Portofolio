package com.example.tamanbacaan.ui.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tamanbacaan.viewmodel.UsersViewModel
import kotlin.math.ceil

@Composable
fun UsersScreen(vm: UsersViewModel = viewModel()) {
    val usersWithBorrows by vm.usersWithBorrows.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Text("Kelola Pengguna", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        if (usersWithBorrows.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(usersWithBorrows, key = { it.user.uid }) { userWithBorrows ->
                val u = userWithBorrows.user
                val userBorrows = userWithBorrows.borrowedBooks

                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.titleMedium)
                                if (u.email.isNotBlank()) Text(u.email, style = MaterialTheme.typography.bodySmall)
                                if (u.isBlocked) Text("TERBLOKIR", color = MaterialTheme.colorScheme.error)
                            }

                            // The blocking functionality will be handled in the ViewModel
                            Button(onClick = { /* TODO: Implement block/unblock in ViewModel */ }) {
                                Text(if (u.isBlocked) "Buka Blokir" else "Blokir")
                            }
                        }

                        if (userBorrows.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Pinjaman:", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            userBorrows.forEach { (borrow, book) ->
                                val now = System.currentTimeMillis()
                                val daysLeft = ceil((borrow.dueDate - now).toDouble() / (1000 * 60 * 60 * 24)).toInt()
                                val status =
                                    if (borrow.returned) " (dikembalikan)"
                                    else if (daysLeft >= 0) " — sisa $daysLeft hari"
                                    else " — terlambat ${-daysLeft} hari"

                                // Now displays the book title
                                Text("• ${book.title}$status", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text("Belum ada pinjaman", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

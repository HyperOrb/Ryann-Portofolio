package com.example.tamanbacaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color


data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = ""
)

@Composable
fun SuperAdminScreen() {

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()

    // ===== FIRESTORE LISTENER =====
    DisposableEffect(Unit) {
        val registration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val rolePriority = mapOf(
                        "superadmin" to 0,
                        "admin" to 1,
                        "user" to 2
                    )

                    users = snapshot.documents
                        .map { doc ->
                            User(
                                uid = doc.id,
                                email = doc.getString("email") ?: "",
                                role = doc.getString("role") ?: "user"
                            )
                        }
                        .sortedWith(
                            compareBy<User> {
                                rolePriority[it.role] ?: 99
                            }.thenBy {
                                it.email.lowercase()
                            }
                        )
                }
            }

        onDispose { registration.remove() }
    }

    // ===== FILTER USERS (EMAIL ONLY) =====
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // ===== UI =====
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8F4))
            .padding(16.dp)
    ) {

        Text(
            text = "Kelola peran pengguna aplikasi",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari berdasarkan email") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredUsers) { user ->
                UserRow(
                    user = user,
                    firestore = firestore
                )
            }
        }
    }
}

@Composable
fun UserRow(
    user: User,
    firestore: FirebaseFirestore
) {
    val roles = listOf("user", "admin")
    var expanded by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    val isSuperAdmin = user.role == "superadmin"

    /* ===== ROLE COLOR SYSTEM ===== */
    val roleColor = when (user.role) {
        "superadmin" -> Color(0xFF7E57C2) // ungu
        "admin" -> Color(0xFF1E88E5)      // biru
        else -> Color(0xFF43A047)         // hijau
    }

    val cardBackground = roleColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /* ===== EMAIL ===== */
            Text(
                text = user.email,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(12.dp))

            /* ===== ROLE BUTTON ===== */
            Box {
                Button(
                    onClick = { expanded = true },
                    enabled = !isSuperAdmin && !isUpdating,
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = roleColor,
                        contentColor = Color.White,
                        disabledContainerColor = roleColor.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isUpdating) "Updating..." else user.role,
                        maxLines = 1
                    )

                    if (!isSuperAdmin && !isUpdating) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }

                if (!isSuperAdmin) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    expanded = false
                                    if (user.role != role) {
                                        isUpdating = true
                                        firestore.collection("users")
                                            .document(user.uid)
                                            .update("role", role)
                                            .addOnCompleteListener {
                                                isUpdating = false
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

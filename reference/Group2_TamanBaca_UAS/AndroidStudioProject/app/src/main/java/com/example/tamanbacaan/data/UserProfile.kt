package com.example.tamanbacaan.data

/**
 * Model data untuk profil user di Firestore.
 */
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user" // Default role is "user"
)

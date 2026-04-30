package com.example.tamanbacaan.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.UserProfileChangeRequest

object AuthRepo {
    // BUKAN field statis: pakai getter supaya tidak menahan Context.
    private val auth get() = Firebase.auth
    private val db get() = Firebase.firestore

    /**
     * Register user baru: buat akun di Firebase Auth lalu simpan profil di Firestore.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        rtRw: String,
        address: String,
        inviteCode: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    callback(false, task.exception?.localizedMessage)
                    return@addOnCompleteListener
                }
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    callback(false, "UID tidak tersedia")
                    return@addOnCompleteListener
                }

                val profile = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "rtRw" to rtRw,
                    "address" to address,
                    "role" to "user"
                )

                if (inviteCode.isNotBlank()) {
                    profile["inviteCode"] = inviteCode
                }

                // Simpan ke Firestore
                db.collection("users").document(uid)
                    .set(profile)
                    .addOnSuccessListener {
                        // Set displayName di FirebaseAuth agar konsisten setelah login ulang
                        auth.currentUser?.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                        )?.addOnCompleteListener { _ ->
                            callback(true, null)
                        }
                    }
                    .addOnFailureListener { e -> callback(false, e.localizedMessage) }
            }
    }

    /**
     * Login user dengan email & password.
     */
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, null)
                else callback(false, task.exception?.localizedMessage)
            }
    }

    /**
     * Sinkronkan FirebaseAuth.displayName dari Firestore user profile.
     * Panggil setelah login supaya nama tidak hilang saat relogin.
     */
    fun syncDisplayName(onDone: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: run { onDone?.invoke(); return }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                if (name.isNullOrBlank()) { onDone?.invoke(); return@addOnSuccessListener }
                val current = auth.currentUser
                if (current != null && current.displayName != name) {
                    current.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    ).addOnCompleteListener { onDone?.invoke() }
                } else {
                    onDone?.invoke()
                }
            }
            .addOnFailureListener { onDone?.invoke() }
    }

    /**
     * Ambil profil user aktif dari Firestore.
     */
    @Suppress("unused") // bakal kepakai saat kamu tampilkan profil
    fun loadMyProfile(callback: (UserProfile?, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(null, "Belum login")
            return
        }
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val p = doc.toObject(UserProfile::class.java)
                    ?: UserProfile(uid = uid, name = "", email = auth.currentUser?.email.orEmpty())
                callback(p, null)
            }
            .addOnFailureListener { e -> callback(null, e.localizedMessage) }
    }

    /**
     * Logout user saat ini.
     */
    @Suppress("unused")
    fun logout() {
        auth.signOut()
    }

    /**
     * Update nama profil: simpan ke Firestore lalu sync ke FirebaseAuth.displayName.
     */
    fun updateProfileName(newName: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val uid = user?.uid
        if (uid == null) {
            callback(false, "Belum login")
            return
        }
        db.collection("users").document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) callback(true, null)
                    else callback(false, task.exception?.localizedMessage)
                }
            }
            .addOnFailureListener { e -> callback(false, e.localizedMessage) }
    }
}

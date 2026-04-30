package com.example.tamanbacaan.ui

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tamanbacaan.R
import com.example.tamanbacaan.data.AuthRepo

private val VALID_INVITE_CODES = setOf("FANTA1", "COLA2", "BEER3")

@Composable
fun RegisterScreen(
    onSubmit: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var rtRw by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var inviteCode by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var loading by rememberSaveable { mutableStateOf(false) }
    var inviteError by rememberSaveable { mutableStateOf<String?>(null) }
    var generalError by rememberSaveable { mutableStateOf<String?>(null) }
    var showSuccess by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun clearErrors() {
        generalError = null
        inviteError = null
    }

    fun validateInvite(codeRaw: String): String? {
        val normalized = codeRaw.trim().uppercase()
        if (normalized.isBlank()) return "Invite code wajib diisi."
        if (normalized !in VALID_INVITE_CODES) return "Kode salah, minta ke petugas."
        return null
    }

    fun validateForm(): Boolean {
        clearErrors()

        val n = name.trim()
        val e = email.trim()
        val p = phone.trim()
        val rr = rtRw.trim()
        val a = address.trim()

        if (n.isBlank()) { generalError = "Nama wajib diisi."; return false }
        if (e.isBlank()) { generalError = "Email wajib diisi."; return false }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { generalError = "Format email tidak valid."; return false }
        if (p.isBlank()) { generalError = "Nomor telepon wajib diisi."; return false }
        if (rr.isBlank()) { generalError = "RW/RT wajib diisi."; return false }
        if (a.isBlank()) { generalError = "Alamat lengkap wajib diisi."; return false }

        val invErr = validateInvite(inviteCode)
        if (invErr != null) { inviteError = invErr; return false }

        if (password.length < 6) { generalError = "Password minimal 6 karakter."; return false }
        if (confirm.isBlank()) { generalError = "Konfirmasi password wajib diisi."; return false }
        if (password != confirm) { generalError = "Konfirmasi password tidak cocok."; return false }

        return true
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                onSubmit()
            },
            title = { Text("Registrasi berhasil 🎉") },
            text = { Text("Akun kamu berhasil dibuat. Silakan login untuk melanjutkan.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    onSubmit()
                }) { Text("OK") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logodlm),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .padding(top = 8.dp)
                )

                Spacer(Modifier.height(8.dp))
                Text("Create Account", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Please fill the form to register.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; generalError = null },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; generalError = null },
                    label = { Text("Nomor Telepon") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = rtRw,
                    onValueChange = { rtRw = it; generalError = null },
                    label = { Text("RW / RT (mis. 05/03)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; generalError = null },
                    label = { Text("Alamat Lengkap") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = {
                        inviteCode = it
                        inviteError = null
                        generalError = null
                    },
                    label = { Text("Invite Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    isError = !inviteError.isNullOrBlank(),
                    supportingText = {
                        if (!inviteError.isNullOrBlank()) {
                            Text(inviteError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "Kode dari petugas",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; generalError = null },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; generalError = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; generalError = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                if (!generalError.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        generalError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!validateForm()) return@Button
                        loading = true

                        val inviteNormalized = inviteCode.trim().uppercase()

                        AuthRepo.register(
                            name.trim(),
                            email.trim(),
                            password,
                            phone.trim(),
                            rtRw.trim(),
                            address.trim(),
                            inviteNormalized
                        ) { ok, msg ->
                            loading = false
                            if (ok) showSuccess = true
                            else generalError = msg ?: "Register gagal."
                        }
                    },
                    enabled = !loading,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(if (loading) "Processing..." else "Sign Up", textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = onSubmit,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !loading
                ) {
                    Text(
                        "Back to Login",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

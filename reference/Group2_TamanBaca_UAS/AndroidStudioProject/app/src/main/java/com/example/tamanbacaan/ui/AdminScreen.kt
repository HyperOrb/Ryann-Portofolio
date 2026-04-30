package com.example.tamanbacaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tamanbacaan.navigation.ROUTE_ADD_BOOK
import com.example.tamanbacaan.navigation.ROUTE_BOOKS
import com.example.tamanbacaan.navigation.ROUTE_CAROUSEL
import com.example.tamanbacaan.navigation.ROUTE_USERS

@Composable
fun AdminScreen(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8F4)) // background lembut
            .padding(16.dp)
    ) {

        /* ===== HEADER ===== */
        Text(
            text = "Admin — area khusus",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Kelola data dan konten aplikasi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        /* ===== ACTION CARDS ===== */
        AdminActionCard(
            title = "Tambah Buku",
            subtitle = "Scan ISBN atau input manual",
            icon = Icons.Default.Add,
            color = Color(0xFF2E7D32)
        ) {
            onNavigate(ROUTE_ADD_BOOK)
        }

        AdminActionCard(
            title = "Daftar Buku",
            subtitle = "Lihat dan hapus buku",
            icon = Icons.Default.MenuBook,
            color = Color(0xFF1E88E5)
        ) {
            onNavigate(ROUTE_BOOKS)
        }

        AdminActionCard(
            title = "Kelola Carousel",
            subtitle = "Atur banner & highlight",
            icon = Icons.Default.ViewCarousel,
            color = Color(0xFF6A1B9A)
        ) {
            onNavigate(ROUTE_CAROUSEL)
        }

        AdminActionCard(
            title = "Kelola Pengguna & Pinjaman",
            subtitle = "User, admin, dan transaksi",
            icon = Icons.Default.Group,
            color = Color(0xFFEF6C00)
        ) {
            onNavigate(ROUTE_USERS)
        }
    }
}

/* ================= ADMIN ACTION CARD ================= */

@Composable
private fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

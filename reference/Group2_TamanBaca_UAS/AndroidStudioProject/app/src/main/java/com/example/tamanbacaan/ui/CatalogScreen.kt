package com.example.tamanbacaan.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.util.LibraryLocationIndicator
import com.example.tamanbacaan.viewmodel.CatalogViewModel
import com.example.tamanbacaan.components.ModernOutlinedButton
import com.example.tamanbacaan.components.SuccessButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel()) {
    val books by viewModel.books.collectAsState()
    val context = LocalContext.current

    val categories = listOf("Semua", "Fiksi", "Non-Fiksi", "Anak", "Teknologi", "Biografi", "Novel", "Komik")
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var isAtLibrary by remember { mutableStateOf(false) }

    val filteredBooks = remember(selectedCategory, books) {
        if (selectedCategory == "Semua") books else books.filter { it.category == selectedCategory }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Katalog Buku",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            // Location indicator (UMN)
            LibraryLocationIndicator(onStatusChanged = { at -> isAtLibrary = at })

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        text = cat,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBooks) { book ->
                    BookItemCard(book) { selectedBook = book }
                }
            }
        }
    }

    selectedBook?.let { book ->
        PremiumBookDetailDialog(
            book = book,
            onDismiss = { selectedBook = null },
            onBorrow = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    if (isAtLibrary) {
                        viewModel.borrowBook(book.id, user.uid)
                        Toast.makeText(context, "Buku berhasil dipinjam!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Anda harus berada di perpustakaan untuk meminjam buku", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Anda harus login untuk meminjam", Toast.LENGTH_SHORT).show()
                }
            },
            isAtLibrary = isAtLibrary
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (selected) 3.dp else 0.dp,
        border = if (selected) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
        modifier = Modifier
            .wrapContentWidth()
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                text = text,
                color = fg,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BookItemCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(220.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
            ) {
                if (book.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }

                // Status Badge
                if (book.isBorrowed) {
                    Surface(
                        color = Color(0xFFF5576C),
                        shape = RoundedCornerShape(bottomStart = 12.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                            Text(
                                text = "Dipinjam",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF38EF7D),
                        shape = RoundedCornerShape(bottomStart = 12.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Tersedia",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = book.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PremiumBookDetailDialog(
    book: Book,
    onDismiss: () -> Unit,
    onBorrow: () -> Unit,
    isAtLibrary: Boolean
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    if (book.coverUrl.isNotBlank()) {
                        AsyncImage(model = book.coverUrl, contentDescription = book.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)))) ,
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.LibraryBooks, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface))))

                    if (book.isBorrowed) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                            Text(text = "Dipinjam", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    } else {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                            Text(text = "Tersedia", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    Text(text = book.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp), color = MaterialTheme.colorScheme.onSurface)

                    if (book.category.isNotBlank() || book.code.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (book.category.isNotBlank()) {
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(6.dp)) {
                                    Text(text = book.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                            }
                            if (book.code.isNotBlank()) {
                                Text(text = "• ${book.code}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (book.description.isNotBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text(text = "Deskripsi", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text(text = book.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernOutlinedButton(
                            text = "Tutup",
                            onClick = onDismiss,
                            icon = Icons.Filled.Close,
                            modifier = Modifier.weight(1f)
                        )

                        SuccessButton(
                            text = "Pinjam",
                            onClick = {
                                if (!isProcessing) {
                                    scope.launch {
                                        isProcessing = true
                                        runCatching {
                                            if (!isAtLibrary) {
                                                Toast.makeText(context, "Anda harus berada di perpustakaan untuk meminjam buku", Toast.LENGTH_SHORT).show()
                                                return@runCatching
                                            }
                                            if (book.isBorrowed) {
                                                Toast.makeText(context, "Buku sudah dipinjam", Toast.LENGTH_SHORT).show()
                                                return@runCatching
                                            }

                                            onBorrow()
                                            kotlinx.coroutines.delay(500)
                                            onDismiss()
                                        }.onFailure { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }

                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing && !book.isBorrowed && isAtLibrary,
                            loading = isProcessing,
                            icon = Icons.Filled.Book,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

package com.example.tamanbacaan.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Repo
import com.example.tamanbacaan.util.DEBUG_SKIP_LOCATION_CHECK
import com.example.tamanbacaan.util.LibraryLocationIndicator
import com.example.tamanbacaan.components.SuccessButton
import com.example.tamanbacaan.components.InfoButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Green palette (base #e8ecdb and complementary greens)
private val PrimaryMidColor = Color(0xFFBFD8A6)
private val PrimaryDarkColor = Color(0xFF7CB17A)
private val AccentGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onReviewsClick: ((String, String) -> Unit)? = null,
    repo: Repo = Repo()
) {
    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isBorrowing by remember { mutableStateOf(false) }
    var isAtLibrary by remember { mutableStateOf(DEBUG_SKIP_LOCATION_CHECK) } // Default to true in debug mode

    // Load book details
    LaunchedEffect(bookId) {
        try {
            val books = repo.listBooks()
            book = books.find { it.id == bookId }
        } catch (e: Exception) {
            android.util.Log.e("BookDetail", "Error loading book: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentBook = book
    if (currentBook == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Text("Buku tidak ditemukan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                InfoButton(
                    text = "Kembali",
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    // Location indicator row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        LibraryLocationIndicator(onStatusChanged = { at -> isAtLibrary = at })
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SuccessButton(
                            text = if (isBorrowing) "Meminjam..." else "Pinjam Buku",
                            onClick = {
                                scope.launch {
                                    isBorrowing = true
                                    try {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        if (user != null) {
                                            if (!isAtLibrary) {
                                                Toast.makeText(context, "Anda harus berada di perpustakaan untuk meminjam buku", Toast.LENGTH_SHORT).show()
                                            } else {
                                                repo.borrowBook(currentBook.id, user.uid)
                                                Toast.makeText(context, "Buku berhasil dipinjam!", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        } else {
                                            Toast.makeText(context, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isBorrowing = false
                                    }
                                }
                            },
                            enabled = !currentBook.isBorrowed && !isBorrowing && isAtLibrary,
                            loading = isBorrowing,
                            icon = Icons.Filled.Book,
                            modifier = Modifier.weight(1f)
                        )

                        // Reviews Button
                        if (onReviewsClick != null) {
                            OutlinedButton(
                                onClick = {
                                    onReviewsClick(currentBook.id, currentBook.title)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    Icons.Filled.RateReview,
                                    contentDescription = "Reviews",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF2196F3)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Reviews",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Book Cover with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (currentBook.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentBook.coverUrl,
                        contentDescription = currentBook.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryMidColor.copy(alpha = 0.3f),
                                        PrimaryDarkColor.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Status Badge
                Surface(
                    color = if (currentBook.isBorrowed) Color(0xFFF5576C) else Color(0xFF38EF7D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .offset(y = 60.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (currentBook.isBorrowed) Icons.Filled.Block else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (currentBook.isBorrowed) "Dipinjam" else "Tersedia",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Book Details Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title and Author
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentBook.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (currentBook.author.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF667EEA)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = currentBook.author,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Rating and Category
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentBook.rating > 0) {
                        Surface(
                            color = Color(0xFFFFA726).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA726),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%.1f", currentBook.rating),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFA726)
                                )
                            }
                        }
                    }

                    if (currentBook.category.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = currentBook.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Book Information Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Informasi Buku",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        if (currentBook.publisher.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.Business,
                                label = "Penerbit",
                                value = currentBook.publisher
                            )
                        }

                        if (currentBook.publishYear.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = "Tahun Terbit",
                                value = currentBook.publishYear
                            )
                        }

                        if (currentBook.isbn.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.QrCode,
                                label = "ISBN",
                                value = currentBook.isbn
                            )
                        }

                        if (currentBook.pages.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.Description,
                                label = "Halaman",
                                value = "${currentBook.pages} halaman"
                            )
                        }

                        if (currentBook.language.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.Language,
                                label = "Bahasa",
                                value = currentBook.language
                            )
                        }

                        if (currentBook.code.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Filled.Tag,
                                label = "Kode Buku",
                                value = currentBook.code
                            )
                        }
                    }
                }

                // Description
                if (currentBook.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Tentang Buku Ini",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = currentBook.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF667EEA).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

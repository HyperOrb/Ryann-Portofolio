package com.example.tamanbacaan.ui.carousel

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import com.example.tamanbacaan.components.ModernOutlinedButton
import com.example.tamanbacaan.components.SuccessButton
import com.example.tamanbacaan.components.DangerButton
import com.example.tamanbacaan.data.CarouselSlide
import com.example.tamanbacaan.data.Repo
import kotlinx.coroutines.launch

// Green palette
private val PrimaryMidColor = Color(0xFFBFD8A6)
private val PrimaryDarkColor = Color(0xFF7CB17A)
private val PrimaryGradient = Brush.horizontalGradient(colors = listOf(PrimaryMidColor, PrimaryDarkColor))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselManagerScreen(
    repo: Repo = Repo(),
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var list by remember { mutableStateOf<List<CarouselSlide>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Form state
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSlide by remember { mutableStateOf<CarouselSlide?>(null) }

    // Load carousel data
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            list = repo.listCarousel()
        } catch (e: Exception) {
            Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryMidColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Kelola Carousel",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Tambah Slide") },
                    containerColor = PrimaryDarkColor,
                    contentColor = Color.White
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ViewCarousel, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Carousel Homepage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${list.size} slide aktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // List of slides
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryDarkColor)
                    }
                } else if (list.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ImageNotSupported,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Belum ada slide carousel",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tekan tombol + untuk menambah slide baru",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(list, key = { it.id }) { slide ->
                            CarouselSlideCard(
                                slide = slide,
                                onEdit = { editingSlide = slide },
                                onDelete = {
                                    scope.launch {
                                        try {
                                            repo.deleteCarouselSlide(slide.id)
                                            list = list.filterNot { it.id == slide.id }
                                            Toast.makeText(ctx, "Slide berhasil dihapus", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
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

    // Add Dialog
    if (showAddDialog) {
        CarouselFormDialog(
            title = "Tambah Slide Baru",
            onDismiss = { showAddDialog = false },
            onSave = { title, description, order, imgBytes ->
                if (imgBytes == null) {
                    Toast.makeText(ctx, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return@CarouselFormDialog
                }
                scope.launch {
                    try {
                        repo.addCarouselSlide(title, description, imgBytes, order)
                        list = repo.listCarousel()
                        showAddDialog = false
                        Toast.makeText(ctx, "✓ Slide berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Edit Dialog
    editingSlide?.let { slide ->
        CarouselFormDialog(
            title = "Edit Slide",
            initialSlide = slide,
            onDismiss = { editingSlide = null },
            onSave = { title, description, order, imgBytes ->
                scope.launch {
                    try {
                        repo.updateCarouselSlide(slide.id, title, description, order, imgBytes)
                        list = repo.listCarousel()
                        editingSlide = null
                        Toast.makeText(ctx, "✓ Slide berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun CarouselSlideCard(
    slide: CarouselSlide,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = slide.imageUrl,
                    contentDescription = slide.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Order badge
                Surface(
                    color = PrimaryDarkColor,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "#${slide.order}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (slide.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hapus")
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Slide?") },
            text = { Text("Yakin ingin menghapus slide \"${slide.title}\"? Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CarouselFormDialog(
    title: String,
    initialSlide: CarouselSlide? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, order: Int, imgBytes: ByteArray?) -> Unit
) {
    val ctx = LocalContext.current

    var slideTitle by remember { mutableStateOf(initialSlide?.title ?: "") }
    var slideDescription by remember { mutableStateOf(initialSlide?.description ?: "") }
    var slideOrder by remember { mutableStateOf(initialSlide?.order?.toString() ?: "0") }
    var imgBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            imgBytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    val isEditing = initialSlide != null
    val canSave = slideTitle.isNotBlank() && (isEditing || imgBytes != null)

    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Image picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 2.dp,
                            color = if (imgBytes != null || initialSlide?.imageUrl?.isNotBlank() == true)
                                PrimaryDarkColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { pickImage.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageUri != null -> {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        initialSlide?.imageUrl?.isNotBlank() == true -> {
                            AsyncImage(
                                model = initialSlide.imageUrl,
                                contentDescription = "Current image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = PrimaryDarkColor
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Pilih Gambar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryDarkColor
                                )
                            }
                        }
                    }

                    // Change image overlay
                    if (imageUri != null || initialSlide?.imageUrl?.isNotBlank() == true) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ganti Gambar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = slideTitle,
                    onValueChange = { slideTitle = it },
                    label = { Text("Judul Slide *") },
                    leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Description field
                OutlinedTextField(
                    value = slideDescription,
                    onValueChange = { slideDescription = it },
                    label = { Text("Keterangan") },
                    leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )

                // Order field
                OutlinedTextField(
                    value = slideOrder,
                    onValueChange = { slideOrder = it.filter { c -> c.isDigit() } },
                    label = { Text("Urutan (angka)") },
                    leadingIcon = { Icon(Icons.Filled.FormatListNumbered, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernOutlinedButton(
                        text = "Batal",
                        onClick = onDismiss,
                        enabled = !saving,
                        icon = Icons.Filled.Close,
                        modifier = Modifier.weight(1f)
                    )

                    SuccessButton(
                        text = if (isEditing) "Simpan" else "Tambah",
                        onClick = {
                            saving = true
                            onSave(
                                slideTitle.trim(),
                                slideDescription.trim(),
                                slideOrder.toIntOrNull() ?: 0,
                                imgBytes
                            )
                        },
                        enabled = canSave && !saving,
                        loading = saving,
                        icon = Icons.Filled.Save,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
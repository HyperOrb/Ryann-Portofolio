package com.example.tamanbacaan.ui.books

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.tamanbacaan.data.Repo
import com.example.tamanbacaan.components.ModernOutlinedButton
import com.example.tamanbacaan.components.SuccessButton
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

// Green palette (consistent with Home/Detail)
private val PrimaryMidColor = Color(0xFFBFD8A6)
private val PrimaryDarkColor = Color(0xFF7CB17A)

private val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryMidColor, PrimaryDarkColor)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    repo: Repo = Repo(),
    onDone: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var publishYear by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var bookDesc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") } // Changed from coverBytes
    var loading by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                            "Tambah Buku Baru",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDone) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tambahkan Buku", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("Lengkapi informasi untuk menambah koleksi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Barcode Scanner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryMidColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = PrimaryDarkColor, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Scan Barcode", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                Text("Scan kode ISBN atau barcode buku", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        PremiumScanButton { detected ->
                            code = detected
                            isbn = detected
                        }
                    }
                }

                // Section: Identitas Buku
                Text(
                    "Identitas Buku",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryDarkColor
                )

                PremiumTextField(value = bookTitle, onValueChange = { bookTitle = it }, label = "Judul Buku *", icon = Icons.Filled.Title, placeholder = "Masukkan judul buku", isError = bookTitle.isBlank() && !loading)
                PremiumTextField(value = author, onValueChange = { author = it }, label = "Penulis", icon = Icons.Filled.Person, placeholder = "Nama penulis")
                PremiumTextField(value = code, onValueChange = { code = it }, label = "Kode Buku", icon = Icons.Filled.Tag, placeholder = "Kode unik buku")
                PremiumTextField(value = isbn, onValueChange = { isbn = it }, label = "ISBN", icon = Icons.Filled.QrCode, placeholder = "Nomor ISBN")

                // Section: Informasi Penerbit
                Text(
                    "Informasi Penerbit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryDarkColor
                )

                PremiumTextField(value = publisher, onValueChange = { publisher = it }, label = "Penerbit", icon = Icons.Filled.Business, placeholder = "Nama penerbit")
                PremiumTextField(value = publishYear, onValueChange = { publishYear = it }, label = "Tahun Terbit", icon = Icons.Filled.CalendarMonth, placeholder = "2024")

                // Section: Detail Buku
                Text(
                    "Detail Buku",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryDarkColor
                )

                PremiumTextField(value = category, onValueChange = { category = it }, label = "Kategori", icon = Icons.Filled.Category, placeholder = "Fiksi, Non-Fiksi, dll")
                PremiumTextField(value = pages, onValueChange = { pages = it }, label = "Jumlah Halaman", icon = Icons.Filled.Description, placeholder = "256")
                PremiumTextField(value = language, onValueChange = { language = it }, label = "Bahasa", icon = Icons.Filled.Language, placeholder = "Indonesia, English, dll")
                PremiumTextField(value = bookDesc, onValueChange = { bookDesc = it }, label = "Deskripsi", icon = Icons.AutoMirrored.Filled.MenuBook, placeholder = "Tulis deskripsi singkat", minLines = 4, maxLines = 6)
                PremiumTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = "URL Cover Gambar", icon = Icons.Filled.Link, placeholder = "https://example.com/image.jpg")

                Spacer(Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernOutlinedButton(
                        text = "Batal",
                        onClick = onDone,
                        enabled = !loading,
                        icon = Icons.Filled.Close,
                        modifier = Modifier.weight(1f)
                    )

                    SuccessButton(
                        text = "Simpan Buku",
                        enabled = !loading && bookTitle.isNotBlank(),
                        loading = loading,
                        onClick = {
                            scope.launch {
                                loading = true
                                try {
                                    repo.addBook(
                                        code = code.trim(),
                                        title = bookTitle.trim(),
                                        desc = bookDesc.trim(),
                                        coverUrl = coverUrl.trim(),
                                        category = category.trim(),
                                        author = author.trim(),
                                        publisher = publisher.trim(),
                                        publishYear = publishYear.trim(),
                                        isbn = isbn.trim(),
                                        pages = pages.trim(),
                                        language = language.trim()
                                    )
                                    Toast.makeText(ctx, "✓ Buku berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                    onDone()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        icon = Icons.Filled.Save,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryDarkColor)
            Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            minLines = minLines,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDarkColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = PrimaryDarkColor.copy(alpha = 0.03f)
            )
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun PremiumScanButton(onResult: (String) -> Unit) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            open = true
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk scan barcode", Toast.LENGTH_SHORT).show()
        }
    }

    SuccessButton(
        text = if (isScanning) "Sedang Scan..." else "Scan Barcode ISBN",
        onClick = {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                open = true
                isScanning = true
            } else {
                askPermission.launch(Manifest.permission.CAMERA)
            }
        },
        enabled = !isScanning,
        loading = isScanning,
        icon = Icons.Filled.QrCodeScanner,
        modifier = Modifier.fillMaxWidth()
    )

    if (open) {
        DisposableEffect(open) {
            onDispose {
                isScanning = false
            }
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = {
            open = false
            isScanning = false
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(PrimaryGradient).padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Scan Barcode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Arahkan ke kode ISBN buku", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            }
                            IconButton(onClick = {
                                open = false
                                isScanning = false
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = Color.White)
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(surfaceProvider)
                                        }

                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        val scanner = BarcodeScanning.getClient()
                                        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            barcode.rawValue?.let { value ->
                                                                onResult(value)
                                                                open = false
                                                                isScanning = false
                                                                Toast.makeText(ctx, "Berhasil scan: $value", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageAnalysis
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay guide
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp, 150.dp)
                                    .background(Color.Transparent)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 4.dp.toPx()
                                    val cornerLength = 40.dp.toPx()

                                    // Top-left corner
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(cornerLength, 0f), strokeWidth)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, cornerLength), strokeWidth)

                                    // Top-right corner
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, cornerLength), strokeWidth)

                                    // Bottom-left corner
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength), androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(cornerLength, size.height), strokeWidth)

                                    // Bottom-right corner
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

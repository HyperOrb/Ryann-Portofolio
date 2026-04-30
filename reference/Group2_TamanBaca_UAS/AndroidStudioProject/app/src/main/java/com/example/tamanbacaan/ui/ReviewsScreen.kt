package com.example.tamanbacaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamanbacaan.data.AuthRepo
import com.example.tamanbacaan.data.Review
import com.example.tamanbacaan.data.Repo
import com.example.tamanbacaan.util.Sentiment
import com.example.tamanbacaan.util.SentimentAnalyzer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    bookId: String,
    bookTitle: String = "",
    onBack: () -> Unit,
    repo: Repo = remember { Repo() },
    sentimentAnalyzer: SentimentAnalyzer? = null // Injected from parent
) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var actualBookTitle by remember { mutableStateOf(bookTitle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isAdmin by remember { mutableStateOf(false) }

    // Load user role to determine visibility of sentiment scores
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            AuthRepo.loadMyProfile { p, _ ->
                isAdmin = (p?.role == "admin" || p?.role == "superadmin")
            }
        } else {
            isAdmin = false
        }
    }

    // Load reviews and book title
    LaunchedEffect(bookId) {
        isLoading = true
        errorMessage = null
        try {
            reviews = repo.listReviewsForBook(bookId)
            // Fetch book title if not provided
            if (actualBookTitle.isEmpty()) {
                val books = repo.listBooks()
                val book = books.find { it.id == bookId }
                actualBookTitle = book?.title ?: "Buku"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Error loading reviews"
            android.util.Log.e("ReviewsScreen", "Error: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    if (showAddReviewDialog && currentUser != null) {
        AddReviewDialog(
            bookId = bookId,
            userId = currentUser.uid,
            userName = currentUser.displayName ?: "Anonymous",
            sentimentAnalyzer = sentimentAnalyzer,
            repo = repo,
            showScore = isAdmin,
            onDismiss = { showAddReviewDialog = false },
            onReviewAdded = {
                scope.launch {
                    reviews = repo.listReviewsForBook(bookId)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Reviews - $actualBookTitle") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.Close, contentDescription = "Back")
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Error memuat reviews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Button(onClick = onBack) {
                        Text("Kembali")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Review Button (jika user logged in)
                item {
                    if (currentUser != null) {
                        Button(
                            onClick = { showAddReviewDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Tulis Review", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sentiment Summary Card
                if (reviews.isNotEmpty()) {
                    item {
                        SentimentSummaryCard(reviews)
                    }
                }

                // Reviews List
                if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada review untuk buku ini.\nJadilah yang pertama!",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(reviews) { review ->
                        ReviewCard(review, showScore = isAdmin)
                    }
                }
            }
        }
    }
}

@Composable
private fun SentimentSummaryCard(reviews: List<Review>) {
    val positiveCount = reviews.count { it.sentiment == Sentiment.POSITIVE.label }
    val negativeCount = reviews.count { it.sentiment == Sentiment.NEGATIVE.label }
    val neutralCount = reviews.count { it.sentiment == Sentiment.NEUTRAL.label }
    val avgRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average().toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ringkasan Review",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Average Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "%.1f / 5.0".format(avgRating),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Text(
                    "(${reviews.size} review)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sentiment Distribution
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SentimentBadge("Positif", positiveCount, Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                SentimentBadge("Netral", neutralCount, Color(0xFF9E9E9E), modifier = Modifier.weight(1f))
                SentimentBadge("Negatif", negativeCount, Color(0xFFf44336), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SentimentBadge(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ReviewCard(review: Review, showScore: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Name + Rating + Sentiment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        review.userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    // Stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = if (index < review.rating) {
                                    Color(0xFFFFB74D)
                                } else {
                                    Color(0xFFE0E0E0)
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Sentiment Badge (only for admin)
                if (showScore && review.sentiment.isNotEmpty()) {
                    SentimentLabel(
                        sentiment = review.sentiment,
                        score = review.sentimentScore,
                        showScore = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment
            Text(
                review.comment,
                fontSize = 12.sp,
                color = Color(0xFF424242),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                formatDate(review.createdAt?.time ?: System.currentTimeMillis()),
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SentimentLabel(sentiment: String, score: Float, showScore: Boolean = false) {
    val (bgColor, textColor) = when (sentiment.uppercase()) {
        "POSITIVE" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "NEGATIVE" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color(0xFF424242)
    }

    Surface(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            if (showScore) "$sentiment\n(${(score * 100).toInt()}%)" else sentiment,
            fontSize = if (showScore) 9.sp else 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(4.dp),
            maxLines = if (showScore) 2 else 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReviewDialog(
    bookId: String,
    userId: String,
    userName: String,
    sentimentAnalyzer: SentimentAnalyzer?,
    repo: Repo,
    showScore: Boolean = false,
    onDismiss: () -> Unit,
    onReviewAdded: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var sentimentResult by remember { mutableStateOf<Pair<String, Float>?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Analyze sentiment real-time
    LaunchedEffect(comment) {
        if (sentimentAnalyzer != null && comment.isNotBlank()) {
            try {
                val result = sentimentAnalyzer.analyzeSentiment(comment)
                sentimentResult = result.sentiment.label to result.score
            } catch (e: Exception) {
                android.util.Log.e("ReviewDialog", "Error analyzing sentiment: ${e.message}", e)
                sentimentResult = null
            }
        } else {
            sentimentResult = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tulis Review") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Rating Selector
                Text("Rating", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) {
                                    Color(0xFFFFB74D)
                                } else {
                                    Color(0xFFE0E0E0)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Comment TextField
                Text("Komentar", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Tulis komentar Anda...") },
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sentiment Preview
                if (sentimentResult != null) {
                    val (sentiment, score) = sentimentResult!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "AI Analisis:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            val (bgColor, textColor) = when (sentiment.uppercase()) {
                                "POSITIVE" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                "NEGATIVE" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                                else -> Color(0xFFF5F5F5) to Color(0xFF424242)
                            }
                            Surface(
                                modifier = Modifier.background(bgColor, shape = RoundedCornerShape(8.dp)),
                                color = bgColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (showScore) "$sentiment (${(score * 100).toInt()}%)" else sentiment,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isBlank()) return@Button
                    isSubmitting = true
                    scope.launch {
                        try {
                            val (sentiment, score) = sentimentResult ?: ("NEUTRAL" to 0.5f)
                            repo.addReview(
                                bookId = bookId,
                                userId = userId,
                                userName = userName,
                                rating = rating,
                                comment = comment,
                                sentiment = sentiment,
                                sentimentScore = score
                            )
                            android.widget.Toast.makeText(
                                context,
                                "Review berhasil ditambahkan!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            onReviewAdded()
                            onDismiss()
                        } catch (e: Exception) {
                            android.util.Log.e("ReviewDialog", "Error submitting review: ${e.message}", e)
                            android.widget.Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = comment.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Mengirim..." else "Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))
    return sdf.format(timestamp)
}

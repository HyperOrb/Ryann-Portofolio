package com.example.tamanbacaan.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamanbacaan.data.Book
import com.example.tamanbacaan.data.Repo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    onBack: () -> Unit,
    repo: Repo = remember { Repo() }
) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var bookStats by remember { mutableStateOf<Map<String, Map<String, Int>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Load books dan sentiment stats
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            books = repo.listBooks()
            val statsMap = mutableMapOf<String, Map<String, Int>>()
            books.forEach { book ->
                val stats = repo.getBookSentimentStats(book.id)
                statsMap[book.id] = stats
            }
            bookStats = statsMap
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Analisis Sentimen Review", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2196F3),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Sentimen Analytics Per Buku",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (books.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada buku",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(books) { book ->
                        val stats = bookStats[book.id] ?: emptyMap()
                        SentimentAnalyticsCard(book, stats)
                    }
                }
            }
        }
    }
}

@Composable
private fun SentimentAnalyticsCard(book: Book, stats: Map<String, Int>) {
    val positiveCount = stats["positive"] ?: 0
    val neutralCount = stats["neutral"] ?: 0
    val negativeCount = stats["negative"] ?: 0
    val totalReviews = positiveCount + neutralCount + negativeCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Book Title
            Text(
                book.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Summary
            if (totalReviews == 0) {
                Text(
                    "Belum ada review",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                // Overall sentiment breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SentimentStatItem(
                        label = "Positif",
                        count = positiveCount,
                        percentage = (positiveCount.toFloat() / totalReviews * 100).toInt(),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp),
                        color = Color.Gray
                    )
                    SentimentStatItem(
                        label = "Netral",
                        count = neutralCount,
                        percentage = (neutralCount.toFloat() / totalReviews * 100).toInt(),
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.weight(1f)
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp),
                        color = Color.Gray
                    )
                    SentimentStatItem(
                        label = "Negatif",
                        count = negativeCount,
                        percentage = (negativeCount.toFloat() / totalReviews * 100).toInt(),
                        color = Color(0xFFf44336),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total reviews
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Review",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Surface(
                        color = Color(0xFF2196F3),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            totalReviews.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sentiment trend interpretation
                val dominantSentiment = when {
                    positiveCount > negativeCount && positiveCount > neutralCount -> {
                        "Positif" to Color(0xFF4CAF50)
                    }
                    negativeCount > positiveCount && negativeCount > neutralCount -> {
                        "Negatif" to Color(0xFFf44336)
                    }
                    else -> "Seimbang" to Color(0xFF9E9E9E)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            dominantSentiment.second.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    color = dominantSentiment.second.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = dominantSentiment.second,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Sentimen dominan: ${dominantSentiment.first}",
                            fontSize = 12.sp,
                            color = dominantSentiment.second,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SentimentStatItem(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = color
        )
        Text(
            "$percentage%",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


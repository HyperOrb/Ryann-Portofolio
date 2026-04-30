package com.example.tamanbacaan.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val sentiment: String = "", // "POSITIVE", "NEUTRAL", "NEGATIVE"
    val sentimentScore: Float = 0f, // 0.0 to 1.0 confidence score
    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    fun isSentimentAnalyzed(): Boolean = sentiment.isNotEmpty()
}


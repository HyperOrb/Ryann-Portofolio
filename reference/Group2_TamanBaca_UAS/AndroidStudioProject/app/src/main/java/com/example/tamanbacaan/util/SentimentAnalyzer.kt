package com.example.tamanbacaan.util

import android.content.Context

enum class Sentiment(val label: String) {
    POSITIVE("POSITIVE"),
    NEUTRAL("NEUTRAL"),
    NEGATIVE("NEGATIVE")
}

data class SentimentResult(
    val sentiment: Sentiment,
    val score: Float // 0.0 to 1.0 confidence
)

class SentimentAnalyzer(private val context: Context) {
    // Positive/Negative keyword lists (ID)
    private val positiveKeywords = setOf(
        "bagus", "baik", "suka", "senang", "mantap", "hebat", "keren",
        "bermanfaat", "menarik", "menghibur", "inspiratif", "asik",
        "memuaskan", "recommended", "bagus sekali", "istimewa", "luar biasa",
        "sempurna", "sukses", "berhasil", "lengkap", "detail", "akurat",
        "tepat", "mudah", "simple", "lancar", "cepat", "responsif"
    )
    private val negativeKeywords = setOf(
        "buruk", "jelek", "seram", "sedih", "kecewa", "kecil", "tidak bagus",
        "tidak baik", "kurang", "jarang", "sulit", "rumit", "bingung",
        "bermasalah", "error", "gagal", "lamban", "berat", "mahal", "murahan",
        "tipis", "pendek", "miskin", "hampa", "kosong", "membosankan",
        "menyebalkan", "mengecewakan"
    )
    private val negations = setOf("tidak", "bukan", "kurang")
    private val intensifiersPos = setOf("sangat", "banget", "sekali", "amat")
    private val intensifiersNeg = setOf("sangat", "banget", "sekali", "amat", "parah")

    fun analyzeSentiment(text: String): SentimentResult {
        if (text.isBlank()) return SentimentResult(Sentiment.NEUTRAL, 0.5f)

        val lower = text.lowercase()
        val words = lower.split(Regex("\\W+")).filter { it.isNotEmpty() }

        var pos = 0
        var neg = 0
        var posBoost = 0
        var negBoost = 0
        var negationCount = 0

        // Count keywords with context adjustments (negations/intensifiers)
        for (i in words.indices) {
            val w = words[i]
            val prev = if (i > 0) words[i - 1] else null
            val isNegated = prev != null && prev in negations

            if (w in positiveKeywords) {
                if (isNegated) neg++ else pos++
                if (!isNegated && (prev != null && prev in intensifiersPos)) posBoost++
            } else if (w in negativeKeywords) {
                if (isNegated) pos++ else neg++
                if (!isNegated && (prev != null && prev in intensifiersNeg)) negBoost++
            }
            if (w in negations) negationCount++
        }

        val length = words.size.coerceAtLeast(1)
        val exclamations = lower.count { it == '!' }
        val emphasis = exclamations.coerceAtMost(3)

        // Base ratios
        val total = (pos + neg).toFloat()
        val baseNeutral = if (total == 0f) 0.6f else 0.3f.coerceAtMost(1f - (total / (length + 4)))
        val posRatio = if (total == 0f) 0f else pos / total
        val negRatio = if (total == 0f) 0f else neg / total

        // Confidence shaping: sigmoid-like curve from ratio difference
        val diff = (posRatio - negRatio)
        var confidence = (0.5f + 0.5f * kotlin.math.tanh(2f * kotlin.math.abs(diff)))

        // Apply boosts and emphasis
        confidence += (posBoost * 0.04f + negBoost * 0.04f)
        confidence += (emphasis * 0.03f) // more exclamations → higher confidence
        confidence -= (negationCount * 0.02f) // many negations reduce confidence

        // Clamp
        confidence = confidence.coerceIn(0.55f, 0.92f)

        // Decide sentiment with soft threshold and minimum confidence floor
        val sentiment = when {
            total == 0f -> Sentiment.NEUTRAL
            posRatio - negRatio > 0.12f -> Sentiment.POSITIVE
            negRatio - posRatio > 0.12f -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }

        // Slightly reduce confidence for neutral to feel less certain
        val finalScore = if (sentiment == Sentiment.NEUTRAL) (confidence - 0.07f).coerceIn(0.45f, 0.85f) else confidence

        return SentimentResult(sentiment, finalScore)
    }

    fun analyzeSentiments(texts: List<String>): List<SentimentResult> {
        return texts.map { analyzeSentiment(it) }
    }
}

package com.example.tamanbacaan.util

/**
 * Unit tests untuk SentimentAnalyzer
 * Gunakan untuk testing sentiment classification
 */
object SentimentAnalyzerTests {

    /**
     * Test cases untuk positive sentiment
     */
    data class TestCase(
        val text: String,
        val expectedSentiment: Sentiment,
        val description: String
    )

    // Test data
    val testCases = listOf(
        // POSITIVE CASES
        TestCase(
            "Buku ini sangat bagus dan menarik!",
            Sentiment.POSITIVE,
            "Strong positive review"
        ),
        TestCase(
            "Aku suka banget buku ini, sangat bermanfaat",
            Sentiment.POSITIVE,
            "Positive with 'suka' keyword"
        ),
        TestCase(
            "Luar biasa! Buku yang sempurna dan inspiratif",
            Sentiment.POSITIVE,
            "Multiple positive keywords"
        ),
        TestCase(
            "Recommended! Ceritanya menghibur dan asik",
            Sentiment.POSITIVE,
            "Positive with entertainment focus"
        ),
        TestCase(
            "Mantap! Penulis yang hebat dan cerita yang keren",
            Sentiment.POSITIVE,
            "Positive with 'mantap' and 'hebat'"
        ),

        // NEGATIVE CASES
        TestCase(
            "Buku ini sangat jelek dan membosankan",
            Sentiment.NEGATIVE,
            "Strong negative review"
        ),
        TestCase(
            "Kecewa dengan buku ini, tidak bagus sama sekali",
            Sentiment.NEGATIVE,
            "Negative with 'kecewa' keyword"
        ),
        TestCase(
            "Buku buruk, ceritanya membuat kesal dan membosankan",
            Sentiment.NEGATIVE,
            "Multiple negative keywords"
        ),
        TestCase(
            "Sulit dipahami, ceritanya rumit dan mengecewakan",
            Sentiment.NEGATIVE,
            "Negative with complexity complaints"
        ),
        TestCase(
            "Error dalam penerbitan, kualitas jelek banget",
            Sentiment.NEGATIVE,
            "Negative quality feedback"
        ),

        // NEUTRAL CASES
        TestCase(
            "Bukunya biasa saja, ada bagus ada kurang",
            Sentiment.NEUTRAL,
            "Mixed positive and negative"
        ),
        TestCase(
            "Cukup bagus tapi ada yang kurang menarik",
            Sentiment.NEUTRAL,
            "Balanced review"
        ),
        TestCase(
            "Buku normal, tidak terlalu bagus tapi tidak jelek",
            Sentiment.NEUTRAL,
            "Neutral stance"
        ),
        TestCase(
            "Buku berhasil tapi ada kendala dalam penceritaan",
            Sentiment.NEUTRAL,
            "Mixed success and failure"
        ),
        TestCase(
            "Bagus di beberapa bagian tapi jelek di bagian lain",
            Sentiment.NEUTRAL,
            "Explicitly mixed"
        )
    )

    /**
     * Run semua test cases dan print hasil
     * Gunakan untuk verify sentiment analyzer accuracy
     */
    fun runAllTests(analyzer: SentimentAnalyzer) {
        println("\n" + "=".repeat(80))
        println("SENTIMENT ANALYZER TEST RESULTS")
        println("=".repeat(80) + "\n")

        var passed = 0
        var failed = 0

        testCases.forEach { test ->
            val result = analyzer.analyzeSentiment(test.text)
            val isPassed = result.sentiment == test.expectedSentiment

            if (isPassed) {
                passed++
                println("✅ PASS")
            } else {
                failed++
                println("❌ FAIL")
            }

            println("  Text: \"${test.text}\"")
            println("  Expected: ${test.expectedSentiment.label}")
            println("  Got: ${result.sentiment.label} (${(result.score * 100).toInt()}%)")
            println("  Description: ${test.description}")
            println()
        }

        println("=".repeat(80))
        println("SUMMARY: $passed passed, $failed failed out of ${testCases.size} tests")
        println("Accuracy: ${(passed.toFloat() / testCases.size * 100).toInt()}%")
        println("=".repeat(80) + "\n")
    }

    /**
     * Test untuk edge cases
     */
    fun testEdgeCases(analyzer: SentimentAnalyzer) {
        println("\n" + "=".repeat(80))
        println("EDGE CASE TESTS")
        println("=".repeat(80) + "\n")

        // Empty string
        val empty = analyzer.analyzeSentiment("")
        println("Empty string: ${empty.sentiment.label} (${(empty.score * 100).toInt()}%)")

        // Single word positive
        val singlePositive = analyzer.analyzeSentiment("bagus")
        println("Single word 'bagus': ${singlePositive.sentiment.label}")

        // Single word negative
        val singleNegative = analyzer.analyzeSentiment("jelek")
        println("Single word 'jelek': ${singleNegative.sentiment.label}")

        // Mixed case
        val mixedCase = analyzer.analyzeSentiment("BAGUS dan JELEK")
        println("Mixed case: ${mixedCase.sentiment.label}")

        // With numbers
        val withNumbers = analyzer.analyzeSentiment("5 bintang, sangat bagus!")
        println("With numbers: ${withNumbers.sentiment.label}")

        // With punctuation
        val withPunct = analyzer.analyzeSentiment("Sangat!!!!! bagus.....!!!!")
        println("With punctuation: ${withPunct.sentiment.label}")

        println("\n" + "=".repeat(80) + "\n")
    }

    /**
     * Performance test - measure latency
     */
    fun performanceTest(analyzer: SentimentAnalyzer, iterations: Int = 1000) {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST ($iterations iterations)")
        println("=".repeat(80) + "\n")

        val testText = "Buku ini sangat bagus dan menarik, aku suka banget!"

        val startTime = System.currentTimeMillis()
        repeat(iterations) {
            analyzer.analyzeSentiment(testText)
        }
        val endTime = System.currentTimeMillis()

        val totalTime = endTime - startTime
        val avgTime = totalTime.toFloat() / iterations

        println("Total time: ${totalTime}ms")
        println("Average time per analysis: ${"%.2f".format(avgTime)}ms")
        println("Throughput: ${"%.0f".format(iterations / (totalTime / 1000.0))} analyses/second")

        println("\n" + "=".repeat(80) + "\n")
    }
}

/**
 * Example penggunaan SentimentAnalyzer dalam real app
 */
object SentimentAnalyzerExamples {

    /**
     * Example 1: Analyze single comment
     */
    fun exampleSingleAnalysis(analyzer: SentimentAnalyzer) {
        val comment = "Buku ini sangat bagus dan bermanfaat, saya sangat merekomendasikan!"
        val result = analyzer.analyzeSentiment(comment)

        println("Comment: $comment")
        println("Sentiment: ${result.sentiment.label}")
        println("Confidence: ${(result.score * 100).toInt()}%")

        when (result.sentiment) {
            Sentiment.POSITIVE -> println("✅ This is positive feedback!")
            Sentiment.NEGATIVE -> println("❌ This is negative feedback!")
            Sentiment.NEUTRAL -> println("➖ This is neutral feedback.")
        }
    }

    /**
     * Example 2: Batch analysis untuk multiple reviews
     */
    fun exampleBatchAnalysis(analyzer: SentimentAnalyzer) {
        val reviews = listOf(
            "Buku yang amazing, top banget!",
            "Biasa saja, tidak terlalu suka",
            "Jelek, waste of time",
            "Bagus tapi ada bagian yang kurang",
            "Recommended untuk semua pembaca"
        )

        val results = analyzer.analyzeSentiments(reviews)

        println("Batch Analysis Results:")
        reviews.zip(results).forEach { (review, result) ->
            println("  \"$review\" → ${result.sentiment.label}")
        }
    }

    /**
     * Example 3: Calculate sentiment statistics
     */
    fun exampleStatistics(analyzer: SentimentAnalyzer) {
        val reviews = listOf(
            "Sangat bagus!",
            "Jelek banget",
            "Cukup oke",
            "Luar biasa menarik",
            "Kecewa dengan buku ini",
            "Bagus dan inspiratif",
            "Tidak memuaskan"
        )

        val results = analyzer.analyzeSentiments(reviews)

        val positiveCount = results.count { it.sentiment == Sentiment.POSITIVE }
        val negativeCount = results.count { it.sentiment == Sentiment.NEGATIVE }
        val neutralCount = results.count { it.sentiment == Sentiment.NEUTRAL }

        val avgScore = results.map { it.score }.average()

        println("Sentiment Statistics:")
        println("  Total reviews: ${reviews.size}")
        println("  Positive: $positiveCount (${(positiveCount.toFloat() / reviews.size * 100).toInt()}%)")
        println("  Neutral: $neutralCount (${(neutralCount.toFloat() / reviews.size * 100).toInt()}%)")
        println("  Negative: $negativeCount (${(negativeCount.toFloat() / reviews.size * 100).toInt()}%)")
        println("  Average confidence: ${(avgScore * 100).toInt()}%")
    }

    /**
     * Example 4: Real-time sentiment update saat user mengetik
     * (Simulasi dari ReviewsScreen behavior)
     */
    fun exampleRealTimeUpdate(analyzer: SentimentAnalyzer) {
        val userInput = "Buku ini..."
        val progressInput = listOf(
            "Buku ini",
            "Buku ini sangat",
            "Buku ini sangat bagus",
            "Buku ini sangat bagus dan",
            "Buku ini sangat bagus dan menarik",
            "Buku ini sangat bagus dan menarik!"
        )

        println("Real-time Sentiment Update (as user types):")
        progressInput.forEach { partial ->
            val result = analyzer.analyzeSentiment(partial)
            println("  \"$partial\" → ${result.sentiment.label} (${(result.score * 100).toInt()}%)")
        }
    }
}


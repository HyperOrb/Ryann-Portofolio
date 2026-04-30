# 📚 Integration Guide: Sentiment Analysis dalam Aplikasi Taman Bacaan

## ✅ Implementasi Checklist

### Step 1: Dependencies ✓
- [x] TensorFlow Lite added to `build.gradle.kts`
  ```kotlin
  implementation("org.tensorflow:tensorflow-lite:2.13.0")
  implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
  ```

### Step 2: Data Models ✓
- [x] `Review.kt` - Data class untuk review dengan sentiment fields
- [x] `Sentiment.kt` (dalam SentimentAnalyzer.kt) - Enum untuk sentiment categories

### Step 3: Business Logic ✓
- [x] `SentimentAnalyzer.kt` - Core sentiment classification
- [x] `Repo.kt` - Database methods untuk review CRUD operations
- [x] Real-time sentiment analysis saat user mengetik

### Step 4: UI Implementation ✓
- [x] `ReviewsScreen.kt` - Main reviews screen dengan:
  - Sentiment summary card
  - Review list dengan sentiment badges
  - Add review dialog
  - Real-time sentiment preview
  
- [x] Updated `BookDetailScreen.kt`:
  - Added "Reviews" button
  - Callback untuk navigate ke ReviewsScreen

### Step 5: Navigation ✓
- [x] Added `ROUTE_REVIEWS` constant di `Routes.kt`
- [x] Updated `NavGraph.kt` dengan ReviewsScreen composable
- [x] Integration dengan BookDetailScreen

---

## 🚀 Cara Menggunakan

### 1. **Akses Review Section**

**Flow User:**
```
1. User buka katalog buku
2. Klik buku yang mau di-review
3. Di BookDetailScreen, ada 2 button: "Pinjam Buku" + "Reviews"
4. Klik "Reviews" → ReviewsScreen terbuka
5. Lihat ringkasan review dan sentiment distribution
6. Klik "Tulis Review" untuk membuat review baru
```

### 2. **Membuat Review Baru**

**Dialog Form:**
- Rating selector (1-5 bintang)
- Comment text field
- Real-time sentiment preview (update saat user mengetik)
- Tombol "Kirim"

**Saat user mengetik:**
```
User input: "Buku ini san..."
→ Sentiment Analyzer analyze
→ Preview update: "POSITIVE (45%)"

User melanjutkan: "...sangat bagus dan menarik"
→ Sentiment Analyzer analyze
→ Preview update: "POSITIVE (87%)"
```

### 3. **Melihat Sentiment Analytics**

Di ReviewsScreen, ada **Sentiment Summary Card** yang menampilkan:
- ⭐ Average rating dari semua review
- 📊 Distribution:
  - Jumlah review POSITIVE
  - Jumlah review NEUTRAL
  - Jumlah review NEGATIVE

---

## 🎓 Untuk Presentasi UAS

### Demo Scenario

```
1. SETUP
   - Open app, login sebagai regular user
   - Navigate ke CatalogScreen
   - Pilih buku sembarang

2. VIEW EXISTING REVIEWS
   - Klik "Reviews" button
   - Show ReviewsScreen dengan existing reviews
   - Highlight sentiment badges dan summary card
   - "Lihat, sistem sudah menganalisis sentimen dari review yang ada"

3. CREATE NEW REVIEW
   - Klik "Tulis Review"
   - Isi rating: 5 bintang
   - Mulai ketik comment sambil menunjukkan real-time sentiment update
   - Ketik: "Buku ini sangat bagus"
   - Preview berubah menjadi: "POSITIVE (87%)"
   - Klik Kirim
   - ReviewsScreen refresh, review baru muncul dengan sentiment label

4. EXPLAIN ALGORITHM
   - Buka SentimentAnalyzer.kt
   - Jelaskan:
     * Keyword-based approach
     * Indonesian keyword database
     * Confidence score calculation
     * Sentimen classification logic
```

### Talking Points

1. **Machine Learning Integration**
   > "Aplikasi mengintegrasikan sentiment analysis untuk memahami opinion pengguna"
   
2. **Real-time Processing**
   > "Saat user mengetik, sistem langsung analyze dan show preview"
   
3. **Data Persistence**
   > "Sentiment dan confidence score disimpan di Firestore untuk analytics"
   
4. **User Experience**
   > "Users mendapat visual feedback tentang sentiment mereka"

---

## 🔌 Code Integration Points

### 1. ReviewsScreen Integration

```kotlin
// Di BookDetailScreen
Button(onClick = { onReviewsClick(bookId, bookTitle) }) {
    Text("Reviews")
}

// onReviewsClick di NavGraph:
onReviewsClick = { bId, bTitle ->
    navController.navigate("book/$bId/reviews")
}
```

### 2. Sentiment Analyzer Usage

```kotlin
// Real-time dalam AddReviewDialog
LaunchedEffect(comment) {
    if (sentimentAnalyzer != null && comment.isNotBlank()) {
        val result = sentimentAnalyzer.analyzeSentiment(comment)
        sentimentResult = result.sentiment.label to result.score
    }
}

// Saat submit review
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
```

### 3. Firestore Schema

```javascript
// Collection: reviews
db.collection("reviews").doc(reviewId)
{
  "id": "review123",
  "bookId": "book456",
  "userId": "user789",
  "userName": "John Doe",
  "rating": 5,
  "comment": "Buku ini sangat bagus dan menarik!",
  "sentiment": "POSITIVE",        // Dari SentimentAnalyzer
  "sentimentScore": 0.87,         // Confidence score
  "createdAt": Timestamp(...),
  "updatedAt": Timestamp(...)
}
```

---

## 📊 Sentiment Statistics API

### Get Summary untuk Buku

```kotlin
val stats = repo.getBookSentimentStats(bookId)
// Returns:
// { 
//   "positive": 8,
//   "neutral": 3,
//   "negative": 1
// }

// Calculate percentages:
val total = stats.values.sum()
val positivePercent = stats["positive"]!! * 100 / total
```

### Real-time Updates

```kotlin
// Flow-based untuk auto-refresh
repo.reviewsForBookFlow(bookId).collect { reviews ->
    // UI update otomatis saat ada review baru
}
```

---

## 🧪 Testing Sentiment Analyzer

### Unit Test Example

```kotlin
@Test
fun testSentimentPositive() {
    val analyzer = SentimentAnalyzer(context)
    val result = analyzer.analyzeSentiment("Buku yang sangat bagus dan menarik!")
    
    assertEquals(Sentiment.POSITIVE, result.sentiment)
    assertTrue(result.score > 0.7f)  // High confidence
}

@Test
fun testSentimentNegative() {
    val analyzer = SentimentAnalyzer(context)
    val result = analyzer.analyzeSentiment("Buku jelek dan membosankan")
    
    assertEquals(Sentiment.NEGATIVE, result.sentiment)
    assertTrue(result.score > 0.7f)
}

@Test
fun testSentimentNeutral() {
    val analyzer = SentimentAnalyzer(context)
    val result = analyzer.analyzeSentiment("Bukunya cukup oke")
    
    assertEquals(Sentiment.NEUTRAL, result.sentiment)
}
```

### Manual Testing via Logs

```kotlin
// Di MainActivity atau debug screen
val analyzer = SentimentAnalyzer(this)
val testCases = listOf(
    "Buku ini sangat bagus!",
    "Jelek dan membosankan",
    "Cukup oke lah"
)

testCases.forEach { text ->
    val result = analyzer.analyzeSentiment(text)
    Log.d("Sentiment", "$text → ${result.sentiment.label} (${result.score})")
}
```

---

## 📱 Features untuk Production

Jika ingin upgrade untuk production, bisa implement:

### 1. **Custom TFLite Model**
```
- Train dengan Indonesian review dataset
- Accuracy: 85-90%
- Latency: ~50ms
```

### 2. **Multi-language Support**
```
- Indonesian (current)
- English, Mandarin, dll.
```

### 3. **Advanced Analytics**
```
- Sentiment trend over time
- Topic extraction dari reviews
- Aspect-based sentiment analysis
```

### 4. **Admin Dashboard**
```
- Visualize sentiment distribution per book
- Filter reviews by sentiment
- Export sentiment reports
```

---

## 📝 Notes untuk UAS

- **Fokus pada**: Real-time sentiment analysis + UI integration
- **Jangan lupa mention**: Keyword-based approach, Firestore storage, real-time updates
- **Demo**: Buat review dengan berbagai sentimen dan show preview updating
- **Code walkthrough**: SentimentAnalyzer.kt → ReviewsScreen.kt → NavGraph.kt

---

**Selesai! Sentimen Analysis sudah fully integrated ke aplikasi Taman Bacaan** ✅

Pertanyaan atau butuh penyesuaian? Tanya aja! 🚀


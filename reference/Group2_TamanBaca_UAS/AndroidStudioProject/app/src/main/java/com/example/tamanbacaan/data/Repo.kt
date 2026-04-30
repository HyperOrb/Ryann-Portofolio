package com.example.tamanbacaan.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.ListenerRegistration

class Repo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    // ==== BOOKS ====
    suspend fun addBook(
        code: String,
        title: String,
        desc: String,
        coverUrl: String,
        category: String = "",
        author: String = "",
        publisher: String = "",
        publishYear: String = "",
        isbn: String = "",
        pages: String = "",
        language: String = ""
    ): String {
        val doc = db.collection("books").document()

        val payload = mapOf(
            "id" to doc.id,
            "code" to code,
            "title" to title,
            "author" to author,
            "publisher" to publisher,
            "publishYear" to publishYear,
            "isbn" to isbn,
            "pages" to pages,
            "language" to language,
            "description" to desc,
            "coverUrl" to coverUrl,
            "createdAt" to System.currentTimeMillis(),
            "category" to category,
            "rating" to 0f,
            "isBorrowed" to false
        )
        doc.set(payload).await()
        return doc.id
    }

    suspend fun listBooks(): List<Book> =
        db.collection("books")
            .orderBy("createdAt")
            .get().await()
            .documents.map {
                Book(
                    id = it.id,
                    code = it.getString("code") ?: "",
                    title = it.getString("title") ?: "",
                    author = it.getString("author") ?: "",
                    publisher = it.getString("publisher") ?: "",
                    publishYear = it.getString("publishYear") ?: "",
                    isbn = it.getString("isbn") ?: "",
                    pages = it.getString("pages") ?: "",
                    language = it.getString("language") ?: "",
                    description = it.getString("description") ?: "",
                    coverUrl = it.getString("coverUrl") ?: "",
                    category = it.getString("category") ?: "",
                    rating = it.getDouble("rating")?.toFloat() ?: 0f,
                    isBorrowed = it.getBoolean("isBorrowed") ?: false
                )
            }

    suspend fun deleteBook(id: String) {
        db.collection("books").document(id).delete().await()
    }

    fun booksFlow(): Flow<List<Book>> = callbackFlow {
        val registration: ListenerRegistration = db.collection("books")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.map {
                        Book(
                            id = it.id,
                            code = it.getString("code") ?: "",
                            title = it.getString("title") ?: "",
                            author = it.getString("author") ?: "",
                            publisher = it.getString("publisher") ?: "",
                            publishYear = it.getString("publishYear") ?: "",
                            isbn = it.getString("isbn") ?: "",
                            pages = it.getString("pages") ?: "",
                            language = it.getString("language") ?: "",
                            description = it.getString("description") ?: "",
                            coverUrl = it.getString("coverUrl") ?: "",
                            category = it.getString("category") ?: "",
                            rating = it.getDouble("rating")?.toFloat() ?: 0f,
                            isBorrowed = it.getBoolean("isBorrowed") ?: false
                        )
                    }
                    trySend(list).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    // ==== CAROUSEL ====
    suspend fun addCarouselSlide(title: String, description: String, imgBytes: ByteArray, order: Int) {
        val doc = db.collection("carousels").document()
        val ref = storage.reference.child("carousel/${doc.id}.jpg")
        ref.putBytes(imgBytes).await()
        val url = ref.downloadUrl.await().toString()
        doc.set(mapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to url,
            "order" to order
        )).await()
    }

    suspend fun updateCarouselSlide(id: String, title: String, description: String, order: Int, imgBytes: ByteArray? = null) {
        val docRef = db.collection("carousels").document(id)

        val updateData = mutableMapOf<String, Any>(
            "title" to title,
            "description" to description,
            "order" to order
        )

        // Jika ada gambar baru, upload dan update URL
        if (imgBytes != null) {
            val ref = storage.reference.child("carousel/$id.jpg")
            ref.putBytes(imgBytes).await()
            val url = ref.downloadUrl.await().toString()
            updateData["imageUrl"] = url
        }

        docRef.update(updateData).await()
    }

    suspend fun listCarousel(): List<CarouselSlide> =
        db.collection("carousels")
            .orderBy("order")
            .get().await()
            .documents.map {
                CarouselSlide(
                    id = it.id,
                    title = it.getString("title") ?: "",
                    description = it.getString("description") ?: "",
                    imageUrl = it.getString("imageUrl") ?: "",
                    order = it.getLong("order")?.toInt() ?: 0
                )
            }

    suspend fun deleteCarouselSlide(id: String) {
        db.collection("carousels").document(id).delete().await()
        try { storage.reference.child("carousel/$id.jpg").delete().await() } catch (_: Exception) {}
    }

    // ==== USERS & BORROWS ====
    suspend fun borrowBook(bookId: String, userId: String) {
        val borrowDocRef = db.collection("borrows").document()
        val bookDocRef = db.collection("books").document(bookId)

        db.runTransaction {
            transaction ->
            val bookSnapshot = transaction.get(bookDocRef)
            if (bookSnapshot.getBoolean("isBorrowed") == true) {
                throw FirebaseFirestoreException("Buku sudah dipinjam.", FirebaseFirestoreException.Code.ABORTED)
            }
            val borrowRecord = Borrow(
                id = borrowDocRef.id,
                bookId = bookId,
                uid = userId,
                startDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000, // 14 days
                returned = false
            )
            transaction.set(borrowDocRef, borrowRecord)
            transaction.update(bookDocRef, "isBorrowed", true)
            null
        }.await()
    }

    suspend fun returnBook(borrowId: String, bookId: String) {
        val borrowDocRef = db.collection("borrows").document(borrowId)
        val bookDocRef = db.collection("books").document(bookId)

        db.runTransaction {
            transaction ->
            transaction.update(borrowDocRef, "returned", true)
            transaction.update(bookDocRef, "isBorrowed", false)
            null
        }.await()
    }

    fun borrowsFlow(uid: String): Flow<List<Borrow>> = callbackFlow {
        val registration = db.collection("borrows").whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val borrows = snapshot.toObjects(Borrow::class.java)
                    trySend(borrows).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun listUsers(): List<AppUser> =
        db.collection("users")
            .get().await()
            .documents.map {
                AppUser(
                    uid = it.id,
                    name = it.getString("name") ?: "",
                    email = it.getString("email") ?: "",
                    isBlocked = it.getBoolean("isBlocked") ?: false
                )
            }

    suspend fun setUserBlocked(uid: String, blocked: Boolean) {
        db.collection("users").document(uid).update("isBlocked", blocked).await()
    }

    suspend fun borrowsByUser(uid: String): List<Borrow> =
        db.collection("borrows")
            .whereEqualTo("uid", uid)
            .get().await()
            .toObjects(Borrow::class.java)

    // ==== REVIEWS (Sentiment Analysis) ====
    suspend fun addReview(
        bookId: String,
        userId: String,
        userName: String,
        rating: Int,
        comment: String,
        sentiment: String = "",
        sentimentScore: Float = 0f
    ): String {
        val doc = db.collection("reviews").document()

        val payload = hashMapOf(
            "id" to doc.id,
            "bookId" to bookId,
            "userId" to userId,
            "userName" to userName,
            "rating" to rating,
            "comment" to comment,
            "sentiment" to sentiment,
            "sentimentScore" to sentimentScore,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        doc.set(payload).await()
        return doc.id
    }

    suspend fun listReviewsForBook(bookId: String): List<Review> =
        db.collection("reviews")
            .whereEqualTo("bookId", bookId)
            .get().await()
            .documents.map {
                Review(
                    id = it.id,
                    bookId = it.getString("bookId") ?: "",
                    userId = it.getString("userId") ?: "",
                    userName = it.getString("userName") ?: "",
                    rating = (it.getLong("rating") ?: 0L).toInt(),
                    comment = it.getString("comment") ?: "",
                    sentiment = it.getString("sentiment") ?: "",
                    sentimentScore = it.getDouble("sentimentScore")?.toFloat() ?: 0f,
                    createdAt = it.getTimestamp("createdAt")?.toDate(),
                    updatedAt = it.getTimestamp("updatedAt")?.toDate()
                )
            }
            .sortedByDescending { it.createdAt }

    fun reviewsForBookFlow(bookId: String): Flow<List<Review>> = callbackFlow {
        val registration = db.collection("reviews")
            .whereEqualTo("bookId", bookId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.documents.map {
                        Review(
                            id = it.id,
                            bookId = it.getString("bookId") ?: "",
                            userId = it.getString("userId") ?: "",
                            userName = it.getString("userName") ?: "",
                            rating = (it.getLong("rating") ?: 0L).toInt(),
                            comment = it.getString("comment") ?: "",
                            sentiment = it.getString("sentiment") ?: "",
                            sentimentScore = it.getDouble("sentimentScore")?.toFloat() ?: 0f,
                            createdAt = it.getTimestamp("createdAt")?.toDate(),
                            updatedAt = it.getTimestamp("updatedAt")?.toDate()
                        )
                    }
                        .sortedByDescending { it.createdAt }
                    trySend(reviews).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Get sentiment statistics untuk sebuah buku
     * Returns: Map dengan keys "positive", "neutral", "negative" dan count-nya
     */
    suspend fun getBookSentimentStats(bookId: String): Map<String, Int> {
        val reviews = listReviewsForBook(bookId)
        val stats = mutableMapOf("positive" to 0, "neutral" to 0, "negative" to 0)

        reviews.forEach { review ->
            when (review.sentiment.uppercase()) {
                "POSITIVE" -> stats["positive"] = stats["positive"]!! + 1
                "NEGATIVE" -> stats["negative"] = stats["negative"]!! + 1
                else -> stats["neutral"] = stats["neutral"]!! + 1
            }
        }

        return stats
    }

    suspend fun deleteReview(reviewId: String) {
        db.collection("reviews").document(reviewId).delete().await()
    }
}
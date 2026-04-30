# 🔍 UNRESOLVED REFERENCE ERROR - TROUBLESHOOTING GUIDE

## ❌ Error: Unresolved reference 'admin'

Jika kamu masih dapat error ini meskipun Routes.kt sudah ada, ikuti langkah-langkah di bawah:

---

## ✅ STEP 1: Verify Routes.kt File

File harus berada di:
```
app/src/main/java/com/example/tamanbacaan/navigation/Routes.kt
```

Isi file harus seperti ini:
```kotlin
package com.example.tamanbacaan.navigation

const val ROUTE_ADMIN       = "admin"
const val ROUTE_ADD_BOOK    = "admin/books/add"
const val ROUTE_BOOKS       = "admin/books"
const val ROUTE_CAROUSEL    = "admin/carousel"
const val ROUTE_USERS       = "admin/users"
const val ROUTE_USER_DETAIL = "admin/users/{uid}"
const val ROUTE_BOOK_DETAIL = "book/{bookId}"
const val ROUTE_REVIEWS     = "book/{bookId}/reviews"
const val ROUTE_ANALYTICS   = "admin/analytics"
const val ROUTE_PROFILE     = "profile"
```

✅ **CHECK:** Pastikan file ini ada dan sudah di-save!

---

## ✅ STEP 2: Verify NavGraph.kt Imports

Di NavGraph.kt, pastikan ada imports ini:

```kotlin
// ROUTES
import com.example.tamanbacaan.navigation.ROUTE_ADMIN
import com.example.tamanbacaan.navigation.ROUTE_ADD_BOOK
import com.example.tamanbacaan.navigation.ROUTE_BOOKS
import com.example.tamanbacaan.navigation.ROUTE_CAROUSEL
import com.example.tamanbacaan.navigation.ROUTE_USERS
import com.example.tamanbacaan.navigation.ROUTE_USER_DETAIL
import com.example.tamanbacaan.navigation.ROUTE_BOOK_DETAIL
import com.example.tamanbacaan.navigation.ROUTE_REVIEWS
import com.example.tamanbacaan.navigation.ROUTE_ANALYTICS
import com.example.tamanbacaan.navigation.ROUTE_PROFILE
```

✅ **CHECK:** Semua 10 constants sudah di-import?

---

## ✅ STEP 3: Clean & Rebuild

### Option A: Manual Clean (Recommended)

1. **Close Android Studio**
2. **Delete these folders:**
   ```
   C:\Users\Ryann\AndroidStudioProjects\PojokBaca\.gradle
   C:\Users\Ryann\AndroidStudioProjects\PojokBaca\.idea
   C:\Users\Ryann\AndroidStudioProjects\PojokBaca\app\build
   C:\Users\Ryann\AndroidStudioProjects\PojokBaca\build
   ```

3. **Re-open Android Studio**
4. **Wait for indexing to complete** (top bar says "Gradle sync finished")
5. **Build → Rebuild Project**

### Option B: Using Android Studio Menu

1. **File → Invalidate Caches...**
2. **Select "Invalidate and Restart"**
3. **Wait untuk Android Studio restart**
4. **Build → Rebuild Project**

---

## ✅ STEP 4: Verify File Structure

Pastikan struktur folder sudah benar:

```
app/src/main/java/com/example/tamanbacaan/
├── navigation/
│   ├── NavGraph.kt          ✅ (should exist)
│   └── Routes.kt            ✅ (should exist)
├── ui/
│   ├── ReviewsScreen.kt     ✅
│   ├── BookDetailScreen.kt  ✅
│   └── admin/
│       └── AdminAnalyticsScreen.kt  ✅
├── data/
│   ├── Review.kt            ✅
│   └── Repo.kt              ✅
└── util/
    ├── SentimentAnalyzer.kt ✅
    └── SentimentAnalyzerTests.kt ✅
```

---

## ✅ STEP 5: Check for Typos

**In NavGraph.kt, line yang pakai ROUTE_ADMIN:**

```kotlin
// ✅ CORRECT
if (isAdmin) add(DrawerItem(ROUTE_ADMIN, "Admin", Icons.Filled.AdminPanelSettings))

// ❌ WRONG (will cause error)
if (isAdmin) add(DrawerItem(admin, "Admin", Icons.Filled.AdminPanelSettings))
if (isAdmin) add(DrawerItem("admin", "Admin", Icons.Filled.AdminPanelSettings))
```

✅ **CHECK:** Gunakan konstanta `ROUTE_ADMIN` bukan string "admin"

---

## 🆘 Jika masih error:

### Option 1: Sync Gradle Files
```
File → Sync Now
```

### Option 2: Restart IDE
```
File → Invalidate Caches and Restart → Invalidate and Restart
```

### Option 3: Check Gradle Sync
Di bagian bawah Android Studio, lihat panel "Gradle" atau "Build"
- Pastikan tidak ada error messages
- Tunggu sampai "Gradle sync finished" muncul

### Option 4: Manual Import
Jika masih tidak kebaca, coba:
1. Di NavGraph.kt, tempat ada error
2. Klik pada nama konstanta (misal: ROUTE_ADMIN)
3. Press `Alt + Enter` (atau `⌥ + Enter` di Mac)
4. Pilih "Import"

---

## 📋 Checklist Akhir

- [ ] Routes.kt file ada di `navigation/` folder
- [ ] Semua 10 ROUTE constants didefinisikan
- [ ] NavGraph.kt punya import untuk 10 constants
- [ ] Tidak ada typo di penggunaan constants
- [ ] `.gradle` dan `.idea` folder sudah dihapus
- [ ] Android Studio sudah di-restart
- [ ] Gradle sync finished (lihat status bar)
- [ ] Build → Rebuild Project berhasil

---

## ✅ Expected Result

Setelah langkah-langkah di atas:
- ❌ Error "Unresolved reference 'admin'" HILANG
- ✅ Project compile tanpa error
- ✅ Semua navigation routes working properly

---

**Jika masih ada issues, screenshot error message dan message aku!** 🚀


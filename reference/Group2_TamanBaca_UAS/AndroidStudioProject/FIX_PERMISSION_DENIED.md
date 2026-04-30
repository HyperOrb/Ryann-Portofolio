# 🔥 CARA FIX ERROR: PERMISSION_DENIED

## ❌ Error yang Muncul:
```
Error memuat reviews
PERMISSION_DENIED: Missing or insufficient permissions.
```

## 🔍 Penyebab:
Firestore Security Rules **tidak mengizinkan** akses ke collection "reviews"

## ✅ Solusi:

### **Opsi 1: Deploy Rules via Firebase Console (RECOMMENDED)**

#### Langkah 1: Buka Firebase Console
1. Buka browser
2. Pergi ke: https://console.firebase.google.com
3. Login dengan akun Google Anda
4. Pilih project **PojokBaca** (atau nama project Anda)

#### Langkah 2: Buka Firestore Rules
1. Di sidebar kiri, klik **"Firestore Database"**
2. Klik tab **"Rules"** di bagian atas
3. Anda akan melihat editor rules

#### Langkah 3: Tambahkan Rules untuk Reviews
Tambahkan code ini **sebelum penutup `}` terakhir**:

```javascript
    // --------- REVIEWS COLLECTION ----------
    // /reviews/{reviewId}
    match /reviews/{reviewId} {
      // Semua orang boleh baca reviews (untuk tampilkan di halaman buku)
      allow read: if true;

      // User yang login boleh membuat review baru
      allow create: if request.auth != null;

      // User hanya boleh edit/hapus review miliknya sendiri
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.userId;

      // Admin boleh edit/hapus semua review
      allow update, delete: if request.auth != null && 
                               (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin" ||
                                get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "superadmin");
    }
```

#### Langkah 4: Publish Rules
1. Klik tombol **"Publish"** di pojok kanan atas
2. Tunggu beberapa detik sampai muncul notifikasi "Rules published successfully"

---

### **Opsi 2: Deploy via Firebase CLI (Advanced)**

Jika Anda sudah install Firebase CLI:

```bash
# Login ke Firebase
firebase login

# Deploy rules
firebase deploy --only firestore:rules
```

---

### **Opsi 3: Temporary Testing Mode (HANYA UNTUK TESTING!)**

**⚠️ WARNING: Jangan gunakan di production! Ini sangat tidak aman!**

Jika hanya untuk testing UAS, Anda bisa temporarily menggunakan:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2025, 12, 31);
    }
  }
}
```

**Tapi ini akan:**
- ❌ Membuka akses ke SEMUA collection untuk SEMUA orang
- ❌ Sangat tidak aman
- ❌ Hanya boleh sampai tanggal yang ditentukan
- ✅ Berguna hanya untuk demo/testing cepat

---

## 📱 SETELAH DEPLOY RULES - Cara Menulis Review:

### **Step 1: Restart Aplikasi**
- Close aplikasi sepenuhnya
- Buka lagi

### **Step 2: Login**
- Login dengan akun user (bukan admin)

### **Step 3: Pilih Buku**
- Dari Home atau Catalog, pilih buku

### **Step 4: Buka Detail Buku**
- Klik pada card buku

### **Step 5: Klik Tombol "Reviews"**
```
┌─────────────────────────────────────┐
│                                     │
│  [Pinjam Buku]    [  Reviews  ]    │ ← KLIK INI!
│                                     │
└─────────────────────────────────────┘
```

### **Step 6: Halaman Reviews Terbuka**
Sekarang Anda akan melihat:
- Tombol **"Tulis Review"** (biru, di atas)
- List reviews (jika sudah ada)
- TIDAK ADA ERROR lagi! ✅

### **Step 7: Klik "Tulis Review"**
Dialog akan muncul dengan:
- ⭐ Rating selector (1-5 bintang)
- 📝 Text field untuk komentar
- 🤖 AI sentiment analysis (real-time)

### **Step 8: Tulis Review**

**Contoh Review Positif:**
```
Rating: ⭐⭐⭐⭐⭐
Komentar: "Buku ini sangat bagus dan menginspirasi! 
          Ceritanya menarik, recommended banget!"
```
→ AI akan detect: 🟢 **POSITIVE (85%)**

**Contoh Review Negatif:**
```
Rating: ⭐⭐
Komentar: "Buku ini membosankan dan mengecewakan. 
          Tidak sesuai ekspektasi."
```
→ AI akan detect: 🔴 **NEGATIVE (80%)**

### **Step 9: Klik "Kirim"**
- Review akan tersimpan
- Toast muncul: "Review berhasil ditambahkan!"
- Review langsung muncul di list

---

## 🎯 Lokasi Fitur Review:

```
HOME/CATALOG
    ↓
PILIH BUKU
    ↓
BOOK DETAIL SCREEN
    ↓ (klik tombol "Reviews")
REVIEWS SCREEN ← DI SINI ANDA BISA:
    │              1. Lihat semua reviews
    │              2. Klik "Tulis Review"
    │              3. Isi form review
    │              4. Submit
    ↓
REVIEW BERHASIL DITAMBAHKAN! ✅
```

---

## 📊 Yang Akan Anda Lihat di Reviews Screen:

### **1. Tombol "Tulis Review"** (biru, di atas)
```
┌─────────────────────────────┐
│  [  Tulis Review  ]         │ ← KLIK untuk menulis
└─────────────────────────────┘
```

### **2. Sentiment Summary Card**
```
┌─────────────────────────────┐
│ Ringkasan Review            │
│ ⭐ 4.5 / 5.0 (3 review)    │
│                             │
│ 🟢 Positif: 2              │
│ ⚪ Netral:  1              │
│ 🔴 Negatif: 0              │
└─────────────────────────────┘
```

### **3. List Reviews**
```
┌─────────────────────────────┐
│ 👤 Budi       🟢 POSITIVE   │
│ ⭐⭐⭐⭐⭐                  │
│ Buku bagus sekali!          │
│ 📅 19 Des 2025 10:30       │
└─────────────────────────────┘

┌─────────────────────────────┐
│ 👤 Ani        🔴 NEGATIVE   │
│ ⭐⭐                        │
│ Kurang menarik              │
│ 📅 19 Des 2025 09:15       │
└─────────────────────────────┘
```

---

## 🐛 Troubleshooting:

### **Jika masih error PERMISSION_DENIED:**
1. ✅ Pastikan rules sudah di-publish di Firebase Console
2. ✅ Tunggu 1-2 menit untuk rules update
3. ✅ Restart aplikasi
4. ✅ Clear app cache
5. ✅ Re-login

### **Jika tidak bisa submit review:**
1. ✅ Pastikan sudah login
2. ✅ Pastikan komentar tidak kosong
3. ✅ Cek koneksi internet
4. ✅ Lihat logcat untuk error detail

### **Jika AI sentiment tidak muncul:**
- Ini normal jika komentar tidak mengandung keyword
- Akan tersimpan sebagai NEUTRAL
- Gunakan kata-kata bahasa Indonesia

---

## 👨‍💼 Admin Analytics:

Setelah ada reviews, admin bisa:
1. Login sebagai admin
2. Buka sidebar (☰)
3. Klik **"Sentimen Analytics"**
4. Lihat statistik untuk semua buku

---

## 📁 File yang Sudah Saya Update:

✅ `firestore.rules` - Sudah ditambahkan rules untuk reviews
✅ Anda tinggal deploy ke Firebase Console

---

## 🚀 Next Steps:

1. **DEPLOY RULES** ke Firebase Console (ikuti Opsi 1 di atas)
2. **Restart aplikasi**
3. **Login** sebagai user
4. **Coba tulis review**!

Selamat mencoba! 🎉


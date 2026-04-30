#!/bin/bash
# Build cleanup script untuk PojokBaca project
# Jalankan ini untuk clean build dan reset caches

echo "════════════════════════════════════════════════════"
echo "🧹 Cleaning PojokBaca Project..."
echo "════════════════════════════════════════════════════"

# 1. Clean Gradle build
echo "1️⃣  Cleaning Gradle build..."
./gradlew clean

# 2. Remove build folder
echo "2️⃣  Removing build folders..."
rm -rf app/build
rm -rf build

# 3. Android Studio cache (jika di Windows)
echo "3️⃣  Clearing Android Studio cache..."
rm -rf .gradle
rm -rf .idea

echo ""
echo "════════════════════════════════════════════════════"
echo "✅ Clean complete! Now:"
echo "════════════════════════════════════════════════════"
echo ""
echo "📱 Steps berikutnya di Android Studio:"
echo "  1. File → Invalidate Caches..."
echo "  2. Select 'Invalidate and Restart'"
echo "  3. Wait untuk Android Studio restart"
echo "  4. Build → Rebuild Project"
echo ""
echo "════════════════════════════════════════════════════"


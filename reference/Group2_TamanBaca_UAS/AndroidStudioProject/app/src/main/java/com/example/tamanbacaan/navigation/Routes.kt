package com.example.tamanbacaan.navigation

const val ROUTE_ADMIN       = "admin"
const val ROUTE_ADD_BOOK    = "admin/books/add"
const val ROUTE_BOOKS       = "admin/books"
const val ROUTE_CAROUSEL    = "admin/carousel"
const val ROUTE_USERS       = "admin/users"
const val ROUTE_USER_DETAIL = "admin/users/{uid}"   // gunakan nav arg uid
const val ROUTE_BOOK_DETAIL = "book/{bookId}"       // route untuk detail buku
const val ROUTE_REVIEWS     = "book/{bookId}/reviews"  // route untuk reviews & sentiment
const val ROUTE_ANALYTICS   = "admin/analytics"     // route untuk sentiment analytics (admin only)
const val ROUTE_PROFILE     = "profile"             // route untuk profile

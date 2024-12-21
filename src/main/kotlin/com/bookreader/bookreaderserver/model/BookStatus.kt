package com.bookreader.bookreaderserver.model

enum class BookStatus {
    AVAILABLE,   // Libro presente e accessibile
    MISSING      // Libro non trovato nel filesystem
}
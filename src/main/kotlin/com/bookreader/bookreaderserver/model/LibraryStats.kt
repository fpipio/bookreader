package com.bookreader.bookreaderserver.model

data class LibraryStats(
    val totalBooks: Int,
    val availableBooks: Int,
    val missingBooks: Int,
    val formatStats: Map<String, Int>,
    val authorStats: Map<String, Int>
)
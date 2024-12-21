package com.bookreader.bookreaderserver.model

import jakarta.persistence.*

@Entity
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var title: String,
    var author: String,
    var path: String,
    var format: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    var status: BookStatus = BookStatus.AVAILABLE,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "library_id")
    var library: Library? = null  // Relazione con Library
)
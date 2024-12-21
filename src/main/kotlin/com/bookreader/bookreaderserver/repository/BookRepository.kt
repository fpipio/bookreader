package com.bookreader.bookreaderserver.repository

import com.bookreader.bookreaderserver.model.Book
import com.bookreader.bookreaderserver.model.Library
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface BookRepository : JpaRepository<Book, Long> {
    fun findByLibrary(library: Library): List<Book>
    fun deleteByLibrary(library: Library)
}
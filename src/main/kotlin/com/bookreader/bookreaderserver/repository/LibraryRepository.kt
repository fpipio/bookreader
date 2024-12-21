package com.bookreader.bookreaderserver.repository

import com.bookreader.bookreaderserver.model.Library
import org.springframework.data.jpa.repository.JpaRepository

interface LibraryRepository : JpaRepository<Library, Long> {
    fun findByPath(path: String): Library?
}
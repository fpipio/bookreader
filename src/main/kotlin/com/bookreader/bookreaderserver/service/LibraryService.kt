package com.bookreader.bookreaderserver.service

import com.bookreader.bookreaderserver.model.Library
import com.bookreader.bookreaderserver.model.LibraryStats
import com.bookreader.bookreaderserver.model.BookStatus
import com.bookreader.bookreaderserver.repository.LibraryRepository
import com.bookreader.bookreaderserver.exception.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.nio.file.Paths
import java.nio.file.InvalidPathException

@Service
class LibraryService(
    private val libraryRepository: LibraryRepository,
    private val bookService: BookService
) {
    fun getAllLibraries(pageable: Pageable): Page<Library> = 
    libraryRepository.findAll(pageable)

    fun getLibraryById(id: Long): Library = libraryRepository.findById(id)
        .orElseThrow { LibraryNotFoundException("Library with id $id not found") }

    @Transactional
    fun createLibrary(library: Library): Library {
        validateLibrary(library)
        
        // Verifica se esiste già una libreria con lo stesso path
        if (libraryRepository.findByPath(library.path) != null) {
            throw DuplicateLibraryException("A library with path ${library.path} already exists")
        }

        return try {
            libraryRepository.save(library)
        } catch (e: Exception) {
            throw LibraryOperationException("Failed to create library: ${e.message}")
        }
    }

    private fun validateLibrary(library: Library) {
        if (library.name.isBlank()) {
            throw LibraryValidationException("Library name cannot be empty")
        }
        if (library.path.isBlank()) {
            throw LibraryValidationException("Library path cannot be empty")
        }
        // Verifica che il path sia un path valido
        try {
            Paths.get(library.path)
        } catch (e: InvalidPathException) {
            throw LibraryValidationException("Invalid library path: ${e.message}")
        }
    }

    @Transactional
    fun scanLibrary(id: Long, forceMetadataUpdate: Boolean): ScanResult {
        val library = getLibraryById(id)
        if (!library.enabled) {
            throw LibraryOperationException("Cannot scan disabled library")
        }
        
        return try {
            bookService.scanAndAddBooks(
                directoryPath = library.path,
                library = library,
                forceMetadataUpdate = forceMetadataUpdate
            )
        } catch (e: Exception) {
            throw LibraryOperationException("Failed to scan library: ${e.message}")
        }
    }

    @Transactional
    fun deleteLibrary(id: Long, deleteBooks: Boolean = false) {
        val library = getLibraryById(id)
        try {
            if (deleteBooks) {
                bookService.deleteBooksByLibrary(library)
            } else {
                bookService.removeLibraryReference(library)
            }
            libraryRepository.delete(library)
        } catch (e: Exception) {
            throw LibraryOperationException("Failed to delete library: ${e.message}")
        }
    }

    fun getLibraryStats(id: Long): LibraryStats {
        val library = getLibraryById(id)
        try {
            val books = bookService.getBooksByLibrary(library)
            return LibraryStats(
                totalBooks = books.size,
                availableBooks = books.count { it.status == BookStatus.AVAILABLE },
                missingBooks = books.count { it.status == BookStatus.MISSING },
                formatStats = books.groupBy { it.format }
                    .mapValues { it.value.size },
                authorStats = books.groupBy { it.author }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(10)
                    .toMap()
            )
        } catch (e: Exception) {
            throw LibraryOperationException("Failed to get library stats: ${e.message}")
        }
    }
}
package com.bookreader.bookreaderserver.service

import com.bookreader.bookreaderserver.model.Book
import com.bookreader.bookreaderserver.model.BookStatus  
import com.bookreader.bookreaderserver.model.Library
import com.bookreader.bookreaderserver.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val fileSystemService: FileSystemService
) {
    // Funzioni CRUD esistenti
    fun getAllBooks(): List<Book> = bookRepository.findAll()

    fun getBookById(id: Long): Book = bookRepository.findById(id)
        .orElseThrow { NoSuchElementException("Book not found") }

    fun getBooksByLibrary(library: Library): List<Book> =
    bookRepository.findByLibrary(library)        

    @Transactional
    fun deleteBooksByLibrary(library: Library) {
        bookRepository.deleteByLibrary(library)
    }

    @Transactional
    fun removeLibraryReference(library: Library) {
        val books = getBooksByLibrary(library)
        books.forEach { book ->
            bookRepository.save(book.copy(library = null))
        }
    }

    @Transactional
    fun createBook(book: Book): Book = bookRepository.save(book)

    @Transactional
    fun updateBook(id: Long, book: Book): Book {
        val existingBook = getBookById(id)
        val updatedBook = existingBook.copy(
            title = book.title,
            author = book.author,
            path = book.path,
            format = book.format,
            description = book.description
        )
        return bookRepository.save(updatedBook)
    }

    @Transactional
    fun deleteBook(id: Long) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id)
        } else {
            throw NoSuchElementException("Book not found")
        }
    }

    // Nuova implementazione della scansione
    @Transactional
    fun scanAndAddBooks(directoryPath: String, library: Library, forceMetadataUpdate: Boolean = false): ScanResult {
        val filesystemBooks = fileSystemService.scanDirectory(directoryPath)
        val databaseBooks = bookRepository.findAll()
        val result = ScanResult()

        filesystemBooks.forEach { bookInfo ->
            val existingBook = databaseBooks.find { it.path == bookInfo.path }

            when {
                existingBook != null && forceMetadataUpdate -> {
                    val updatedBook = existingBook.copy(
                        title = bookInfo.title,
                        author = bookInfo.author,
                        description = bookInfo.description,
                        status = BookStatus.AVAILABLE,
                        library = library  // Aggiungi riferimento alla libreria
                    )
                    bookRepository.save(updatedBook)
                    result.updated++
                }
                existingBook != null -> {
                    result.unchanged++
                }
                else -> {
                    val newBook = Book(
                        title = bookInfo.title,
                        author = bookInfo.author,
                        path = bookInfo.path,
                        format = bookInfo.format,
                        description = bookInfo.description,
                        status = BookStatus.AVAILABLE,
                        library = library  // Aggiungi riferimento alla libreria
                    )
                    bookRepository.save(newBook)
                    result.added++
                }
            }
        }

        // Aggiorna solo i libri che appartengono a questa libreria
        databaseBooks
            .filter { it.library?.id == library.id }
            .forEach { dbBook ->
                if (filesystemBooks.none { it.path == dbBook.path } && dbBook.status != BookStatus.MISSING) {
                    val updatedBook = dbBook.copy(status = BookStatus.MISSING)
                    bookRepository.save(updatedBook)
                    result.missing++
                }
            }

        return result
    }
}

// Questa classe può rimanere qui
data class ScanResult(
    var added: Int = 0,      
    var updated: Int = 0,    
    var unchanged: Int = 0,  
    var missing: Int = 0     
)

// Aggiungi questo enum
enum class BookStatus {
    AVAILABLE,   // Libro presente e accessibile
    MISSING      // Libro non trovato nel filesystem
}
package com.bookreader.bookreaderserver.controller

import com.bookreader.bookreaderserver.model.Book
import com.bookreader.bookreaderserver.service.BookService
import com.bookreader.bookreaderserver.exception.BookReaderException
import com.bookreader.bookreaderserver.exception.InvalidPathException
import com.bookreader.bookreaderserver.exception.FileAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {

    @GetMapping
    fun getAllBooks(): List<Book> = bookService.getAllBooks()

    @GetMapping("/{id}")
    fun getBookById(@PathVariable id: Long): Book = bookService.getBookById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody book: Book): Book = bookService.createBook(book)

    @PutMapping("/{id}")
    fun updateBook(@PathVariable id: Long, @RequestBody book: Book): Book = 
        bookService.updateBook(id, book)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBook(@PathVariable id: Long) = bookService.deleteBook(id)

    @ExceptionHandler(BookReaderException::class)
    fun handleBookReaderException(e: BookReaderException): ResponseEntity<ErrorResponse> {
        println("DEBUG - Exception caught: ${e.message}")
        val errorResponse = ErrorResponse(
            code = when (e) {
                is InvalidPathException -> "INVALID_PATH"
                is FileAccessException -> "FILE_ACCESS"
            },
            message = e.message ?: "Errore sconosciuto"
        )
        
        val status = when (e) {
            is InvalidPathException -> HttpStatus.BAD_REQUEST
            is FileAccessException -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity(errorResponse, status)
    }

    data class ErrorResponse(
        val code: String,
        val message: String
    )

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to (e.message ?: "Risorsa non trovata")), HttpStatus.NOT_FOUND)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String>> {
        println("DEBUG - Unexpected error: ${e.message}")
        return ResponseEntity(
            mapOf("error" to "Si è verificato un errore interno"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
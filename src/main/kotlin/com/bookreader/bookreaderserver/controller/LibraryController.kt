package com.bookreader.bookreaderserver.controller

import com.bookreader.bookreaderserver.model.Library
import com.bookreader.bookreaderserver.model.LibraryStats
import com.bookreader.bookreaderserver.service.LibraryService
import com.bookreader.bookreaderserver.service.ScanResult
import com.bookreader.bookreaderserver.exception.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/libraries")
@Tag(name = "Library Controller", description = "API per la gestione delle librerie")
class LibraryController(private val libraryService: LibraryService) {
    
    @GetMapping
    @Operation(
        summary = "Ottiene tutte le librerie",
        description = "Restituisce una lista paginata di tutte le librerie"
    )
    fun getAllLibraries(
        @Parameter(description = "Numero di pagina (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Dimensione della pagina")
        @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Campo per l'ordinamento")
        @RequestParam(defaultValue = "name") sortBy: String,
        @Parameter(description = "Direzione dell'ordinamento (ASC/DESC)")
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): Page<Library> {
        val sort = Sort.by(Sort.Direction.valueOf(sortDirection.uppercase()), sortBy)
        val pageable = PageRequest.of(page, size, sort)
        return libraryService.getAllLibraries(pageable)
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Ottiene una libreria per ID")
    @ApiResponse(responseCode = "200", description = "Libreria trovata")
    @ApiResponse(responseCode = "404", description = "Libreria non trovata")
    fun getLibraryById(@PathVariable id: Long): Library =
        libraryService.getLibraryById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una nuova libreria")
    fun createLibrary(@Valid @RequestBody library: Library): Library = 
        libraryService.createLibrary(library)
        
    @PostMapping("/{id}/scan")
    @Operation(
        summary = "Scansiona una libreria",
        description = "Scansiona il contenuto di una libreria e aggiorna il database"
    )
    fun scanLibrary(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") forceMetadataUpdate: Boolean
    ): ScanResult = libraryService.scanLibrary(id, forceMetadataUpdate)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una libreria")
    fun deleteLibrary(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") deleteBooks: Boolean
    ) {
        libraryService.deleteLibrary(id, deleteBooks)
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Ottiene le statistiche di una libreria")
    fun getLibraryStats(@PathVariable id: Long): LibraryStats =
        libraryService.getLibraryStats(id)

    @ExceptionHandler(LibraryException::class)
    fun handleLibraryException(e: LibraryException): ResponseEntity<ErrorResponse> {
        val (status, code) = when (e) {
            is LibraryNotFoundException -> HttpStatus.NOT_FOUND to "LIBRARY_NOT_FOUND"
            is DuplicateLibraryException -> HttpStatus.CONFLICT to "DUPLICATE_LIBRARY"
            is LibraryValidationException -> HttpStatus.BAD_REQUEST to "INVALID_LIBRARY"
            is LibraryOperationException -> HttpStatus.INTERNAL_SERVER_ERROR to "LIBRARY_OPERATION_FAILED"
        }

        val errorResponse = ErrorResponse(
            code = code,
            message = e.message ?: "An error occurred with the library operation"
        )

        return ResponseEntity(errorResponse, status)
    }

    data class ErrorResponse(
        val code: String,
        val message: String
    )
}
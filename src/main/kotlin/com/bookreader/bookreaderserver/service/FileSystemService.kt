package com.bookreader.bookreaderserver.service

import com.bookreader.bookreaderserver.exception.FileAccessException
import com.bookreader.bookreaderserver.exception.InvalidPathException
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

@Service
class FileSystemService(private val metadataService: MetadataService) {
    fun scanDirectory(directoryPath: String): List<BookInfo> {
        println("DEBUG - Attempting to scan directory: $directoryPath")
        
        val path = try {
            Paths.get(directoryPath)
        } catch (e: java.nio.file.InvalidPathException) {
            println("DEBUG - Invalid path syntax: ${e.message}")
            throw InvalidPathException("Il percorso contiene caratteri non validi: '${e.message}'")
        }

        try {
            if (!Files.exists(path)) {
                throw InvalidPathException("La directory '$directoryPath' non esiste")
            }

            if (!Files.isDirectory(path)) {
                throw InvalidPathException("'$directoryPath' non è una directory")
            }

            return Files.walk(path)
                .filter { Files.isRegularFile(it) }
                .filter { it.toString().lowercase().let { path ->
                    path.endsWith(".epub") || path.endsWith(".pdf") || 
                    path.endsWith(".cbz") || path.endsWith(".cbr")
                }}
                .map { filePath -> createBookInfo(filePath) }
                .toList()
        } catch (e: SecurityException) {
            throw FileAccessException("Accesso negato alla directory '$directoryPath'")
        } catch (e: Exception) {
            println("DEBUG - Scan error: ${e.message}")
            throw FileAccessException("Errore durante la scansione di '$directoryPath': ${e.message}")
        }
    }

    private fun createBookInfo(path: Path): BookInfo {
        return when {
            path.toString().lowercase().endsWith(".epub") -> {
                val metadata = metadataService.extractEpubMetadata(path)
                BookInfo(
                    title = metadata.title,
                    author = metadata.author,
                    path = path.toString(),
                    format = "EPUB",
                    description = metadata.description
                )
            }
            path.toString().lowercase().endsWith(".pdf") -> BookInfo(
                title = path.fileName.toString().substringBeforeLast("."),
                author = "Unknown",
                path = path.toString(),
                format = "PDF",
                description = null
            )
            path.toString().lowercase().endsWith(".cbz") -> BookInfo(
                title = path.fileName.toString().substringBeforeLast("."),
                author = "Unknown",
                path = path.toString(),
                format = "CBZ",
                description = null
            )
            else -> BookInfo(
                title = path.fileName.toString().substringBeforeLast("."),
                author = "Unknown",
                path = path.toString(),
                format = "CBR",
                description = null
            )
        }
    }
}

data class BookInfo(
    val title: String,
    val author: String,
    val path: String,
    val format: String,
    val description: String?
)
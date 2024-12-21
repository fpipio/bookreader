package com.bookreader.bookreaderserver.service

import nl.siegmann.epublib.domain.Book as EpubBook
import nl.siegmann.epublib.epub.EpubReader
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.nio.file.Path

@Service
class MetadataService {
    private val epubReader = EpubReader()

    fun extractEpubMetadata(filePath: Path): BookMetadata {
        val epub = FileInputStream(filePath.toFile()).use { inputStream ->
            epubReader.readEpub(inputStream)
        }
        
        return BookMetadata(
            title = epub.title ?: filePath.fileName.toString().substringBeforeLast("."),
            author = epub.metadata.authors.firstOrNull()?.toString() ?: "Unknown",
            description = epub.metadata.descriptions.firstOrNull()
        )
    }
}

data class BookMetadata(
    val title: String,
    val author: String,
    val description: String? = null
)
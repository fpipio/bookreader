package com.bookreader.bookreaderserver.exception

sealed class BookReaderException(message: String) : RuntimeException(message)

class InvalidPathException(message: String) : BookReaderException(message)

class FileAccessException(message: String) : BookReaderException(message)
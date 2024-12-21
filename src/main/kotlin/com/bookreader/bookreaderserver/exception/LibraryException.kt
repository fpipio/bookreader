package com.bookreader.bookreaderserver.exception

sealed class LibraryException(message: String) : RuntimeException(message)

class LibraryNotFoundException(message: String) : LibraryException(message)
class DuplicateLibraryException(message: String) : LibraryException(message)
class LibraryOperationException(message: String) : LibraryException(message)
class LibraryValidationException(message: String) : LibraryException(message)
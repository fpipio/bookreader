package com.bookreader.bookreaderserver.model

data class ScanRequest(
    val directory: String,
    val forceMetadataUpdate: Boolean = false  
)
// src/main/kotlin/com/bookreader/bookreaderserver/model/Library.kt
package com.bookreader.bookreaderserver.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Schema(description = "Modello che rappresenta una libreria di libri")
data class Library(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Il nome della libreria non può essere vuoto")
    @field:Size(min = 3, max = 100, message = "Il nome deve essere tra 3 e 100 caratteri")
    var name: String,

    @field:NotBlank(message = "Il path non può essere vuoto")
    @field:Size(max = 500, message = "Il path non può superare i 500 caratteri")
    var path: String,

    var enabled: Boolean = true,

    @Column(columnDefinition = "TEXT")
    var description: String? = null
)
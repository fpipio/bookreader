package com.bookreader.bookreaderserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.bookreader.bookreaderserver"])
class BookReaderServerApplication

fun main(args: Array<String>) {
    runApplication<BookReaderServerApplication>(*args)
}
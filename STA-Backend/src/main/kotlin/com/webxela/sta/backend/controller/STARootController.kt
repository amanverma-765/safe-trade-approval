package com.webxela.sta.backend.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/docs")
class STARootController {

    @RequestMapping("", "/")
    @Throws(IOException::class)
    fun serveApiDocs(): ResponseEntity<Any?> {
        val htmlFile = ClassPathResource("api-docs/sta.html")
        val path = Paths.get(htmlFile.uri)
        val htmlContent = Files.readString(path)

        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_HTML
        return ResponseEntity<Any?>(htmlContent, headers, HttpStatus.OK)
    }
}
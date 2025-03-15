package com.webxela.sta.backend.utils

import java.util.Base64

fun encodeAppNumber(applicationNumber: String): String {

    // For each digit, add 39 to it and convert the result to a character.
    val encodedMiddle = applicationNumber.map { ch ->
        val digit = Character.getNumericValue(ch)
        (digit + 39).toChar()
    }.joinToString("")

    // Define the wrapper string. Note that the backslash is escaped.
    val wrapper = "XYZ[\\]"

    // Combine the wrapper, the encoded middle, and the wrapper again.
    val combined = wrapper + encodedMiddle + wrapper

    // Base64 encode the final string.
    val encodedBytes = Base64.getEncoder().encode(combined.toByteArray(Charsets.UTF_8))
    return String(encodedBytes, Charsets.UTF_8)
}
